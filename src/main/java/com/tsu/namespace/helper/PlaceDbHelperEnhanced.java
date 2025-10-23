package com.tsu.namespace.helper;

import com.tsu.auth.security.AppSecurityContext;
import com.tsu.common.jpa.JsonValueUtils;
import com.tsu.namespace.entities.PlaceAltNameTb;
import com.tsu.namespace.entities.PlaceTb;
import com.tsu.namespace.entities.id.PlaceId;
import com.tsu.namespace.record.PlaceAltNameRecord;
import com.tsu.namespace.record.PlaceRecordEnhanced;
import com.tsu.namespace.repo.PlaceAltNameRepository;
import com.tsu.namespace.repo.PlaceRepositoryEnhanced;
import com.tsu.namespace.repo.spec.PlaceSpecification;
import com.tsu.place.request.PlaceFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Enhanced database helper for Place operations with comprehensive search and geo-spatial support.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceDbHelperEnhanced {

    private static final int SRID = 4326; // WGS84
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);

    private final PlaceRepositoryEnhanced placeRepository;
    private final PlaceAltNameRepository altNameRepository;

    // ========== CRUD Operations ==========

    public PlaceRecordEnhanced createPlace(UUID namespaceId, String type, String name,
                                          String isoCountry, String country, String county, String city,
                                          String building, String address, String postCode,
                                          Double lat, Double lng, UUID parentId,
                                          Object props, String notes, AppSecurityContext context) {
        log.debug("Creating place: namespaceId={}, type={}, name={}", namespaceId, type, name);

        PlaceTb tb = new PlaceTb();
        PlaceId id = new PlaceId(namespaceId, UUID.randomUUID());
        tb.setId(id);
        tb.setParentId(parentId);
        tb.setType(type);
        tb.setIsoCountry(validateIsoCountry(isoCountry));
        tb.setCountry(country);
        tb.setCounty(county);
        tb.setCity(city);
        tb.setBuilding(building);
        tb.setAddress(address);
        tb.setPostCode(postCode);
        tb.setName(name);
        tb.setLat(lat);
        tb.setLng(lng);

        // Create PostGIS geometry if coordinates provided
        if (lat != null && lng != null) {
            Point point = createPoint(lng, lat);
            tb.setCenter(point);
            log.debug("Created geometry point: SRID={}, coordinates=({}, {})", SRID, lng, lat);
        }

        tb.setProps(JsonValueUtils.getInstance().encodeAsJson(props));
        tb.setNotes(notes);
        tb.setActive(true);
        tb.setCreatedBy(context.getPrincipal().id());
        tb.setCreatedDate(LocalDateTime.now());
        tb.setModifiedBy(context.getPrincipal().id());
        tb.setModifiedDate(LocalDateTime.now());

        placeRepository.save(tb);
        log.info("Place created with id: {}", id.getId());

        return build(tb, context);
    }

    public PlaceRecordEnhanced updatePlace(UUID namespaceId, UUID placeId, String name,
                                          String address, Double lat, Double lng,
                                          Object props, String notes, AppSecurityContext context) {
        PlaceId id = new PlaceId(namespaceId, placeId);
        PlaceTb tb = placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Place not found: " + placeId));

        if (name != null) tb.setName(name);
        if (address != null) tb.setAddress(address);
        if (lat != null) {
            tb.setLat(lat);
            if (tb.getLng() != null) {
                tb.setCenter(createPoint(tb.getLng(), lat));
            }
        }
        if (lng != null) {
            tb.setLng(lng);
            if (tb.getLat() != null) {
                tb.setCenter(createPoint(lng, tb.getLat()));
            }
        }
        if (props != null) tb.setProps(JsonValueUtils.getInstance().encodeAsJson(props));
        if (notes != null) tb.setNotes(notes);

        tb.setModifiedBy(context.getPrincipal().id());
        tb.setModifiedDate(LocalDateTime.now());

        placeRepository.save(tb);
        log.info("Place updated: {}", placeId);

        return build(tb, context);
    }

    public Optional<PlaceRecordEnhanced> findPlaceById(UUID namespaceId, UUID placeId, AppSecurityContext context) {
        PlaceId id = new PlaceId(namespaceId, placeId);
        return placeRepository.findById(id)
                .map(tb -> build(tb, context));
    }

    public PlaceRecordEnhanced setPlaceActive(UUID namespaceId, UUID placeId, boolean active, AppSecurityContext context) {
        PlaceId id = new PlaceId(namespaceId, placeId);
        PlaceTb tb = placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Place not found: " + placeId));

        tb.setActive(active);
        tb.setModifiedBy(context.getPrincipal().id());
        tb.setModifiedDate(LocalDateTime.now());

        placeRepository.save(tb);
        log.info("Place {} {}", placeId, active ? "activated" : "deactivated");

        return build(tb, context);
    }

    public void deletePlace(UUID namespaceId, UUID placeId) {
        PlaceId id = new PlaceId(namespaceId, placeId);
        placeRepository.deleteById(id);
        log.info("Place deleted: {}", placeId);
    }

    // ========== Search Operations ==========

    public Page<PlaceRecordEnhanced> search(UUID namespaceId, PlaceFilter filter, Pageable pageable, AppSecurityContext context) {
        Specification<PlaceTb> spec = PlaceSpecification.fromFilter(namespaceId, filter);
        return placeRepository.findAll(spec, pageable)
                .map(tb -> build(tb, context));
    }

    public Page<PlaceRecordEnhanced> searchByNamePrefix(UUID namespaceId, String prefix, Pageable pageable, AppSecurityContext context) {
        return placeRepository.searchByNamePrefix(namespaceId, prefix, pageable)
                .map(tb -> build(tb, context));
    }

    public List<PlaceRecordEnhanced> searchByNameFuzzy(UUID namespaceId, String query, int limit, AppSecurityContext context) {
        return placeRepository.searchByNameFuzzy(namespaceId, query, limit)
                .stream()
                .map(tb -> build(tb, context))
                .toList();
    }

    public List<PlaceRecordEnhanced> searchPlacesByName(UUID namespaceId, String query, String type,
                                                        String isoCountry, int limit, AppSecurityContext context) {
        return placeRepository.searchPlacesByName(namespaceId, query, type, isoCountry, limit)
                .stream()
                .map(tb -> build(tb, context))
                .toList();
    }

    public Page<PlaceRecordEnhanced> findByType(UUID namespaceId, String type, boolean activeOnly,
                                                Pageable pageable, AppSecurityContext context) {
        if (activeOnly) {
            return placeRepository.findByNamespaceIdAndTypeAndActiveTrue(namespaceId, type, pageable)
                    .map(tb -> build(tb, context));
        } else {
            return placeRepository.findByNamespaceIdAndType(namespaceId, type, pageable)
                    .map(tb -> build(tb, context));
        }
    }

    // ========== Geo-Spatial Queries ==========

    public List<PlaceRecordEnhanced> findPlacesWithinRadius(UUID namespaceId, double lat, double lng,
                                                            double radiusMeters, AppSecurityContext context) {
        log.debug("Finding places within {} meters of ({}, {})", radiusMeters, lat, lng);
        return placeRepository.findPlacesWithinRadius(namespaceId, lat, lng, radiusMeters)
                .stream()
                .map(tb -> build(tb, context))
                .toList();
    }

    public List<PlaceRecordEnhanced> findNearestPlaces(UUID namespaceId, double lat, double lng,
                                                       int limit, AppSecurityContext context) {
        log.debug("Finding {} nearest places to ({}, {})", limit, lat, lng);
        return placeRepository.findNearestPlaces(namespaceId, lat, lng, limit)
                .stream()
                .map(tb -> build(tb, context))
                .toList();
    }

    public List<PlaceRecordEnhanced> findPlacesWithinBbox(UUID namespaceId, double minLat, double maxLat,
                                                          double minLng, double maxLng, AppSecurityContext context) {
        log.debug("Finding places within bbox: ({}, {}) to ({}, {})", minLat, minLng, maxLat, maxLng);
        return placeRepository.findPlacesWithinBbox(namespaceId, minLat, maxLat, minLng, maxLng)
                .stream()
                .map(tb -> build(tb, context))
                .toList();
    }

    public List<PlaceRecordEnhanced> reverseGeocode(UUID namespaceId, double lat, double lng,
                                                    int maxResults, AppSecurityContext context) {
        log.debug("Reverse geocoding point ({}, {})", lat, lng);
        return placeRepository.reverseGeocode(namespaceId, lat, lng, maxResults)
                .stream()
                .map(tb -> build(tb, context))
                .toList();
    }

    public Optional<Double> calculateDistance(UUID namespaceId, UUID placeId1, UUID placeId2) {
        log.debug("Calculating distance between places {} and {}", placeId1, placeId2);
        return Optional.ofNullable(placeRepository.calculateDistance(namespaceId, placeId1, placeId2));
    }

    // ========== Hierarchy Queries ==========

    public Page<PlaceRecordEnhanced> findChildren(UUID namespaceId, UUID parentId,
                                                  Pageable pageable, AppSecurityContext context) {
        return placeRepository.findByNamespaceIdAndParentId(namespaceId, parentId, pageable)
                .map(tb -> build(tb, context));
    }

    public List<PlaceRecordEnhanced> findAllDescendants(UUID namespaceId, UUID parentId, AppSecurityContext context) {
        return placeRepository.findAllDescendants(namespaceId, parentId)
                .stream()
                .map(tb -> build(tb, context))
                .toList();
    }

    public List<PlaceRecordEnhanced> findAncestors(UUID namespaceId, UUID placeId, AppSecurityContext context) {
        return placeRepository.findAncestors(namespaceId, placeId)
                .stream()
                .map(tb -> build(tb, context))
                .toList();
    }

    public Page<PlaceRecordEnhanced> findRootPlaces(UUID namespaceId, Pageable pageable, AppSecurityContext context) {
        return placeRepository.findByNamespaceIdAndParentIdIsNull(namespaceId, pageable)
                .map(tb -> build(tb, context));
    }

    // ========== Alternate Names ==========

    public void addAlternateName(UUID namespaceId, UUID placeId, String name, String lang,
                                String type, AppSecurityContext context) {
        PlaceAltNameTb tb = new PlaceAltNameTb();
        tb.setNamespaceId(namespaceId);
        tb.setPlaceId(placeId);
        tb.setName(name);
        tb.setLang(lang);
        tb.setType(type);
        tb.setCreatedBy(context.getPrincipal().id());
        tb.setCreatedDate(LocalDateTime.now());

        altNameRepository.save(tb);
        log.info("Alternate name added for place {}: {} ({})", placeId, name, lang);
    }

    public List<PlaceAltNameRecord> getAlternateNames(UUID namespaceId, UUID placeId, AppSecurityContext context) {
        return altNameRepository.findByNamespaceIdAndPlaceId(namespaceId, placeId)
                .stream()
                .map(tb -> buildAltName(tb, context))
                .toList();
    }

    public List<PlaceAltNameRecord> getAlternateNamesByLanguage(UUID namespaceId, UUID placeId,
                                                                String lang, AppSecurityContext context) {
        return altNameRepository.findByNamespaceIdAndPlaceIdAndLang(namespaceId, placeId, lang)
                .stream()
                .map(tb -> buildAltName(tb, context))
                .toList();
    }

    public void deleteAlternateName(UUID namespaceId, Long altNameId) {
        altNameRepository.deleteById(new com.tsu.namespace.entities.id.PlaceAltNameId(namespaceId, altNameId));
        log.info("Alternate name deleted: {}", altNameId);
    }

    // ========== Helper Methods ==========

    private String validateIsoCountry(String isoCountry) {
        if (isoCountry == null || isoCountry.isEmpty()) {
            return null;
        }
        String code = isoCountry.toUpperCase();
        if (code.length() != 2) {
            log.warn("ISO country code '{}' is not ISO 3166-1 alpha-2 format. Expected 2 characters.", isoCountry);
        }
        return code;
    }

    private Point createPoint(double longitude, double latitude) {
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }

        Coordinate coordinate = new Coordinate(longitude, latitude);
        Point point = geometryFactory.createPoint(coordinate);
        point.setSRID(SRID);
        return point;
    }

    private PlaceRecordEnhanced build(PlaceTb tb, AppSecurityContext context) {
        return new PlaceRecordEnhanced(tb, context);
    }

    private PlaceAltNameRecord buildAltName(PlaceAltNameTb tb, AppSecurityContext context) {
        return new PlaceAltNameRecord(tb, context);
    }
}
