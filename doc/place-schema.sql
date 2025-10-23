-- Enhanced Place Schema with PostGIS support
-- Multi-tenant location management with hierarchy, geo-search, and reverse geocoding

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS btree_gist;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Main Place table with composite ID
DROP TABLE IF EXISTS place CASCADE;
CREATE TABLE place (
    namespace_id     UUID NOT NULL,
    id               UUID NOT NULL,

    -- Hierarchy
    parent_id        UUID,                            -- Self-reference for hierarchy (building > floor > room)

    -- Classification
    type             TEXT NOT NULL,                   -- country|region|city|neighborhood|poi|campus|building|floor|room|route|area|water
    iso_country      CHAR(2),                         -- ISO-3166-1 alpha-2 (for countries or inferred)

    -- Address components
    country          TEXT,
    county           TEXT,
    city             TEXT,
    building         TEXT,
    address          TEXT,
    post_code        TEXT,

    -- Primary name
    name             TEXT NOT NULL,

    -- Geo coordinates
    lat              DOUBLE PRECISION,
    lng              DOUBLE PRECISION,

    -- PostGIS geometry (WGS84 SRID 4326)
    center           GEOGRAPHY(Point, 4326),          -- Center point for distance/radius queries
    bbox             GEOMETRY(Polygon, 4326),         -- Bounding box for area-based filtering

    -- Additional data
    props            JSONB,                           -- Extensible properties
    notes            TEXT,

    -- Status
    active           BOOLEAN NOT NULL DEFAULT true,

    -- Audit (standard naming convention)
    created_by       UUID NOT NULL,
    created_date     TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by      UUID NOT NULL,
    modified_date    TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (namespace_id, id),

    CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
        REFERENCES namespace(id) ON DELETE CASCADE,
    CONSTRAINT parent_fk FOREIGN KEY(namespace_id, parent_id)
        REFERENCES place(namespace_id, id) ON DELETE CASCADE,
    CONSTRAINT created_by_fk FOREIGN KEY(created_by)
        REFERENCES base_principal(id),
    CONSTRAINT updated_by_fk FOREIGN KEY(updated_by)
        REFERENCES base_principal(id)
);

-- Alternate names for multi-language support and search
DROP TABLE IF EXISTS place_alt_name CASCADE;
CREATE TABLE place_alt_name (
    namespace_id     UUID NOT NULL,
    id               BIGSERIAL,
    place_id         UUID NOT NULL,

    name             TEXT NOT NULL,
    lang             VARCHAR(10),                     -- ISO 639-1 language code (en, zh, ja, etc.)
    type             TEXT,                            -- official|colloquial|historic|abbreviation

    created_by       UUID NOT NULL,
    created_date     TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (namespace_id, id),

    CONSTRAINT place_fk FOREIGN KEY(namespace_id, place_id)
        REFERENCES place(namespace_id, id) ON DELETE CASCADE
);

-- Indexes for performance

-- Primary name search (prefix + fuzzy)
CREATE INDEX place_name_idx ON place (namespace_id, name);
CREATE INDEX place_name_trgm_idx ON place USING gin (name gin_trgm_ops);
CREATE INDEX place_name_lower_idx ON place (namespace_id, LOWER(name));

-- Classification filters
CREATE INDEX place_type_idx ON place (namespace_id, type);
CREATE INDEX place_iso_country_idx ON place (namespace_id, iso_country);
CREATE INDEX place_country_idx ON place (namespace_id, country);
CREATE INDEX place_post_code_idx ON place (namespace_id, post_code);
CREATE INDEX place_active_idx ON place (namespace_id, active);

-- Hierarchy queries
CREATE INDEX place_parent_idx ON place (namespace_id, parent_id);

-- Geo-spatial indexes (critical for performance)
CREATE INDEX place_center_gist_idx ON place USING gist (center);
CREATE INDEX place_bbox_gist_idx ON place USING gist (bbox);

-- Composite indexes for common queries
CREATE INDEX place_type_active_idx ON place (namespace_id, type, active);
CREATE INDEX place_country_type_idx ON place (namespace_id, country, type);

-- JSONB properties search
CREATE INDEX place_props_gin_idx ON place USING gin (props);

-- Alternate names indexes
CREATE INDEX place_alt_name_place_idx ON place_alt_name (namespace_id, place_id);
CREATE INDEX place_alt_name_name_idx ON place_alt_name (namespace_id, name);
CREATE INDEX place_alt_name_name_trgm_idx ON place_alt_name USING gin (name gin_trgm_ops);
CREATE INDEX place_alt_name_lang_idx ON place_alt_name (namespace_id, lang);

-- Function for reverse geocoding (point-in-polygon)
-- Find the most specific place containing a point (prioritizes smaller areas)
CREATE OR REPLACE FUNCTION reverse_geocode(
    p_namespace_id UUID,
    p_longitude DOUBLE PRECISION,
    p_latitude DOUBLE PRECISION,
    p_max_results INT DEFAULT 10
)
RETURNS TABLE (
    place_id UUID,
    place_type TEXT,
    place_name TEXT,
    distance_meters DOUBLE PRECISION
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.id,
        p.type,
        p.name,
        ST_Distance(
            p.center::geography,
            ST_SetSRID(ST_MakePoint(p_longitude, p_latitude), 4326)::geography
        ) as dist
    FROM place p
    WHERE
        p.namespace_id = p_namespace_id
        AND p.active = true
        AND (
            -- Point in bounding box (if exists)
            p.bbox IS NULL
            OR ST_Contains(p.bbox, ST_SetSRID(ST_MakePoint(p_longitude, p_latitude), 4326))
        )
    ORDER BY
        -- Prioritize by area (smaller areas first), then by distance
        CASE
            WHEN p.bbox IS NOT NULL THEN ST_Area(p.bbox::geography)
            ELSE 999999999999
        END ASC,
        dist ASC
    LIMIT p_max_results;
END;
$$ LANGUAGE plpgsql STABLE;

-- Function for fuzzy name search with ranking
CREATE OR REPLACE FUNCTION search_place_by_name(
    p_namespace_id UUID,
    p_query TEXT,
    p_type TEXT DEFAULT NULL,
    p_iso_country CHAR(2) DEFAULT NULL,
    p_limit INT DEFAULT 20
)
RETURNS TABLE (
    place_id UUID,
    place_name TEXT,
    place_type TEXT,
    place_country TEXT,
    similarity_score REAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.id,
        p.name,
        p.type,
        p.country,
        GREATEST(
            similarity(p.name, p_query),
            similarity(LOWER(p.name), LOWER(p_query))
        ) as score
    FROM place p
    WHERE
        p.namespace_id = p_namespace_id
        AND p.active = true
        AND (p_type IS NULL OR p.type = p_type)
        AND (p_iso_country IS NULL OR p.iso_country = p_iso_country)
        AND (
            p.name ILIKE p_query || '%'  -- Prefix match
            OR p.name % p_query           -- Trigram similarity
            OR LOWER(p.name) ILIKE '%' || LOWER(p_query) || '%'  -- Contains
        )
    ORDER BY
        -- Prioritize exact prefix matches, then similarity
        CASE WHEN p.name ILIKE p_query || '%' THEN 1 ELSE 2 END,
        score DESC,
        p.name
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql STABLE;

-- Comments for documentation
COMMENT ON TABLE place IS 'Multi-tenant location/place storage with PostGIS support for geo-spatial queries';
COMMENT ON COLUMN place.center IS 'Point geometry for distance and radius queries (WGS84)';
COMMENT ON COLUMN place.bbox IS 'Bounding box polygon for area-based filtering and containment checks';
COMMENT ON TABLE place_alt_name IS 'Alternate names for places (translations, historical names, abbreviations)';
