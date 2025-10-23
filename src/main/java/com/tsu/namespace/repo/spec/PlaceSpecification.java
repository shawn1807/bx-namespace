package com.tsu.namespace.repo.spec;

import com.tsu.namespace.entities.PlaceTb;
import com.tsu.place.request.PlaceFilter;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Specification for complex Place queries.
 * Provides type-safe, composable query building for place search.
 */
@Slf4j
public class PlaceSpecification {

    private static final int SRID = 4326; // WGS84
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);

    /**
     * Builds a complete specification from PlaceFilter.
     */
    public static Specification<PlaceTb> fromFilter(UUID namespaceId, PlaceFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Namespace isolation (always required)
            predicates.add(cb.equal(root.get("namespaceId"), namespaceId));

            // Active filter (default to true if not specified)
            if (filter.getActiveOnly() == null || filter.getActiveOnly()) {
                predicates.add(cb.isTrue(root.get("active")));
            }

            // Name search
            if (filter.getQuery() != null && !filter.getQuery().isEmpty()) {
                addNameSearchPredicate(predicates, root, cb, filter.getQuery(), filter.getSearchMode());
            }

            // Type filters
            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }
            if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
                predicates.add(root.get("type").in(filter.getTypes()));
            }

            // Classification filters
            if (filter.getIsoCountry() != null) {
                predicates.add(cb.equal(root.get("isoCountry"), filter.getIsoCountry()));
            }
            if (filter.getCountry() != null) {
                predicates.add(cb.equal(root.get("country"), filter.getCountry()));
            }
            if (filter.getCity() != null) {
                predicates.add(cb.equal(root.get("city"), filter.getCity()));
            }
            if (filter.getPostCode() != null) {
                predicates.add(cb.equal(root.get("postCode"), filter.getPostCode()));
            }

            // Hierarchy filters
            if (filter.getParentId() != null) {
                if (filter.getIncludeDescendants() != null && filter.getIncludeDescendants()) {
                    // Recursive CTE would be needed for full descendants - simplified here
                    log.warn("includeDescendants requires recursive query - using direct children only");
                }
                predicates.add(cb.equal(root.get("parentId"), filter.getParentId()));
            }

            // Geo-spatial filters (handled separately in repository for native queries)
            // These predicates are for basic filtering; complex geo queries use native SQL

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Name search predicate based on search mode.
     */
    private static void addNameSearchPredicate(List<Predicate> predicates, Root<PlaceTb> root,
                                              CriteriaBuilder cb, String query, PlaceFilter.SearchMode mode) {
        PlaceFilter.SearchMode searchMode = mode != null ? mode : PlaceFilter.SearchMode.PREFIX;

        switch (searchMode) {
            case EXACT:
                predicates.add(cb.equal(root.get("name"), query));
                break;
            case PREFIX:
                predicates.add(cb.like(cb.lower(root.get("name")), query.toLowerCase() + "%"));
                break;
            case CONTAINS:
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%"));
                break;
            case FUZZY:
                // Fuzzy search requires native PostgreSQL functions - handled in repository
                log.debug("Fuzzy search requires native SQL - falling back to CONTAINS");
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%"));
                break;
        }
    }

    /**
     * Specification for places within a namespace.
     */
    public static Specification<PlaceTb> hasNamespaceId(UUID namespaceId) {
        return (root, query, cb) -> cb.equal(root.get("namespaceId"), namespaceId);
    }

    /**
     * Specification for active places only.
     */
    public static Specification<PlaceTb> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    /**
     * Specification for places of a specific type.
     */
    public static Specification<PlaceTb> hasType(String type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    /**
     * Specification for places in a specific country.
     */
    public static Specification<PlaceTb> hasIsoCountry(String isoCountry) {
        return (root, query, cb) -> cb.equal(root.get("isoCountry"), isoCountry);
    }

    /**
     * Specification for places with a specific parent.
     */
    public static Specification<PlaceTb> hasParent(UUID parentId) {
        return (root, query, cb) -> cb.equal(root.get("parentId"), parentId);
    }

    /**
     * Specification for root-level places (no parent).
     */
    public static Specification<PlaceTb> isRootLevel() {
        return (root, query, cb) -> cb.isNull(root.get("parentId"));
    }

    /**
     * Specification for name prefix search (case-insensitive).
     */
    public static Specification<PlaceTb> nameStartsWith(String prefix) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), prefix.toLowerCase() + "%");
    }

    /**
     * Specification for name contains search (case-insensitive).
     */
    public static Specification<PlaceTb> nameContains(String text) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + text.toLowerCase() + "%");
    }

    /**
     * Helper to create a PostGIS Point from lat/lng.
     */
    public static Point createPoint(double longitude, double latitude) {
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(SRID);
        return point;
    }
}
