package com.tsu.namespace.repo;

import com.tsu.namespace.entities.PlaceTb;
import com.tsu.namespace.entities.id.PlaceId;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Enhanced Place repository with PostGIS geo-spatial queries,
 * full-text search, and hierarchy support.
 */
@Repository
public interface PlaceRepositoryEnhanced extends JpaRepository<PlaceTb, PlaceId>, JpaSpecificationExecutor<PlaceTb> {

    // ========== Basic CRUD ==========

    Optional<PlaceTb> findByNamespaceIdAndId(UUID namespaceId, UUID id);

    Page<PlaceTb> findByNamespaceId(UUID namespaceId, Pageable pageable);

    Page<PlaceTb> findByNamespaceIdAndActiveTrue(UUID namespaceId, Pageable pageable);

    List<PlaceTb> findByNamespaceIdAndParentId(UUID namespaceId, UUID parentId);

    // ========== Name Search ==========

    /**
     * Prefix search (case-insensitive, fast with index).
     */
    @Query("SELECT p FROM PlaceTb p WHERE p.namespaceId = :namespaceId " +
           "AND p.active = true " +
           "AND LOWER(p.name) LIKE LOWER(CONCAT(:prefix, '%')) " +
           "ORDER BY p.name")
    Page<PlaceTb> searchByNamePrefix(@Param("namespaceId") UUID namespaceId,
                                     @Param("prefix") String prefix,
                                     Pageable pageable);

    /**
     * Fuzzy search using PostgreSQL trigram similarity.
     * Returns results sorted by similarity score (most similar first).
     */
    @Query(value = "SELECT p.*, similarity(p.name, :query) as sim_score " +
                   "FROM place p " +
                   "WHERE p.namespace_id = :namespaceId " +
                   "AND p.active = true " +
                   "AND (p.name % :query OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
                   "ORDER BY sim_score DESC, p.name " +
                   "LIMIT :limit",
           nativeQuery = true)
    List<PlaceTb> searchByNameFuzzy(@Param("namespaceId") UUID namespaceId,
                                    @Param("query") String query,
                                    @Param("limit") int limit);

    /**
     * Use the database function for optimized fuzzy search with filters.
     */
    @Query(value = "SELECT p.* FROM search_place_by_name(" +
                   ":namespaceId, :query, :type, :isoCountry, :limit) s " +
                   "JOIN place p ON p.namespace_id = :namespaceId AND p.id = s.place_id",
           nativeQuery = true)
    List<PlaceTb> searchPlacesByName(@Param("namespaceId") UUID namespaceId,
                                     @Param("query") String query,
                                     @Param("type") String type,
                                     @Param("isoCountry") String isoCountry,
                                     @Param("limit") int limit);

    // ========== Classification Filters ==========

    Page<PlaceTb> findByNamespaceIdAndType(UUID namespaceId, String type, Pageable pageable);

    Page<PlaceTb> findByNamespaceIdAndTypeAndActiveTrue(UUID namespaceId, String type, Pageable pageable);

    Page<PlaceTb> findByNamespaceIdAndIsoCountry(UUID namespaceId, String isoCountry, Pageable pageable);

    Page<PlaceTb> findByNamespaceIdAndCountry(UUID namespaceId, String country, Pageable pageable);

    List<PlaceTb> findByNamespaceIdAndPostCode(UUID namespaceId, String postCode);

    // ========== Geo-Spatial Queries ==========

    /**
     * Find places within radius (meters) of a point.
     * Uses PostGIS geography for accurate WGS84 distance calculation.
     */
    @Query(value = "SELECT p.* FROM place p " +
                   "WHERE p.namespace_id = :namespaceId " +
                   "AND p.active = true " +
                   "AND p.center IS NOT NULL " +
                   "AND ST_DWithin(p.center, " +
                   "  ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radiusMeters) " +
                   "ORDER BY ST_Distance(p.center, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography)",
           nativeQuery = true)
    List<PlaceTb> findPlacesWithinRadius(@Param("namespaceId") UUID namespaceId,
                                         @Param("lat") double lat,
                                         @Param("lng") double lng,
                                         @Param("radiusMeters") double radiusMeters);

    /**
     * Find K-nearest places to a point.
     */
    @Query(value = "SELECT p.*, " +
                   "ST_Distance(p.center, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography) as distance " +
                   "FROM place p " +
                   "WHERE p.namespace_id = :namespaceId " +
                   "AND p.active = true " +
                   "AND p.center IS NOT NULL " +
                   "ORDER BY distance " +
                   "LIMIT :limit",
           nativeQuery = true)
    List<PlaceTb> findNearestPlaces(@Param("namespaceId") UUID namespaceId,
                                    @Param("lat") double lat,
                                    @Param("lng") double lng,
                                    @Param("limit") int limit);

    /**
     * Find places within a bounding box.
     */
    @Query(value = "SELECT p.* FROM place p " +
                   "WHERE p.namespace_id = :namespaceId " +
                   "AND p.active = true " +
                   "AND p.center IS NOT NULL " +
                   "AND ST_Within(p.center::geometry, " +
                   "  ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326))",
           nativeQuery = true)
    List<PlaceTb> findPlacesWithinBbox(@Param("namespaceId") UUID namespaceId,
                                       @Param("minLat") double minLat,
                                       @Param("maxLat") double maxLat,
                                       @Param("minLng") double minLng,
                                       @Param("maxLng") double maxLng);

    /**
     * Reverse geocode: find places containing a point.
     * Uses the database function for optimized reverse geocoding.
     */
    @Query(value = "SELECT p.* FROM reverse_geocode(:namespaceId, :lng, :lat, :maxResults) r " +
                   "JOIN place p ON p.namespace_id = :namespaceId AND p.id = r.place_id",
           nativeQuery = true)
    List<PlaceTb> reverseGeocode(@Param("namespaceId") UUID namespaceId,
                                 @Param("lat") double lat,
                                 @Param("lng") double lng,
                                 @Param("maxResults") int maxResults);

    /**
     * Calculate distance between two places (in meters).
     */
    @Query(value = "SELECT ST_Distance(" +
                   "  (SELECT center FROM place WHERE namespace_id = :namespaceId AND id = :id1), " +
                   "  (SELECT center FROM place WHERE namespace_id = :namespaceId AND id = :id2)" +
                   ")",
           nativeQuery = true)
    Double calculateDistance(@Param("namespaceId") UUID namespaceId,
                            @Param("id1") UUID id1,
                            @Param("id2") UUID id2);

    // ========== Hierarchy Queries ==========

    /**
     * Find root-level places (no parent).
     */
    Page<PlaceTb> findByNamespaceIdAndParentIdIsNull(UUID namespaceId, Pageable pageable);

    /**
     * Find direct children of a place.
     */
    Page<PlaceTb> findByNamespaceIdAndParentId(UUID namespaceId, UUID parentId, Pageable pageable);

    /**
     * Find all descendants (recursive query).
     * Uses PostgreSQL recursive CTE for hierarchy traversal.
     */
    @Query(value = "WITH RECURSIVE descendants AS ( " +
                   "  SELECT * FROM place WHERE namespace_id = :namespaceId AND parent_id = :parentId " +
                   "  UNION ALL " +
                   "  SELECT p.* FROM place p " +
                   "  INNER JOIN descendants d ON p.parent_id = d.id AND p.namespace_id = d.namespace_id " +
                   ") " +
                   "SELECT * FROM descendants",
           nativeQuery = true)
    List<PlaceTb> findAllDescendants(@Param("namespaceId") UUID namespaceId,
                                     @Param("parentId") UUID parentId);

    /**
     * Find ancestors of a place (bottom-up hierarchy).
     */
    @Query(value = "WITH RECURSIVE ancestors AS ( " +
                   "  SELECT * FROM place WHERE namespace_id = :namespaceId AND id = :placeId " +
                   "  UNION ALL " +
                   "  SELECT p.* FROM place p " +
                   "  INNER JOIN ancestors a ON p.id = a.parent_id AND p.namespace_id = a.namespace_id " +
                   ") " +
                   "SELECT * FROM ancestors WHERE id != :placeId",
           nativeQuery = true)
    List<PlaceTb> findAncestors(@Param("namespaceId") UUID namespaceId,
                                @Param("placeId") UUID placeId);

    // ========== Combined Queries ==========

    /**
     * Find places by type within radius.
     */
    @Query(value = "SELECT p.* FROM place p " +
                   "WHERE p.namespace_id = :namespaceId " +
                   "AND p.active = true " +
                   "AND p.type = :type " +
                   "AND p.center IS NOT NULL " +
                   "AND ST_DWithin(p.center, " +
                   "  ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radiusMeters) " +
                   "ORDER BY ST_Distance(p.center, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography)",
           nativeQuery = true)
    List<PlaceTb> findPlacesByTypeWithinRadius(@Param("namespaceId") UUID namespaceId,
                                               @Param("type") String type,
                                               @Param("lat") double lat,
                                               @Param("lng") double lng,
                                               @Param("radiusMeters") double radiusMeters);
}
