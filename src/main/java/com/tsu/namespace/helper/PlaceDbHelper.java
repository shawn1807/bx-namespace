package com.tsu.namespace.helper;

import com.tsu.namespace.entities.PlaceTb;
import com.tsu.namespace.record.PlaceRecord;
import com.tsu.namespace.repo.PlaceRepository;
import com.tsu.common.jpa.JsonValueUtils;
import com.tsu.auth.security.AppSecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceDbHelper {

    private static final int SRID = 4326; // WGS84
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);
    private final PlaceRepository placeRepository;

    public Optional<PlaceRecord> findPlaceById(Integer id, AppSecurityContext context) {
        return placeRepository.findById(id)
                .map(tb -> build(tb, context));
    }

    public PlaceRecord addPlace(String country, String county, String city, String building, String address, String postCode,
                           double lat, double lng, String notes, Object props, AppSecurityContext context) {
        log.debug("Adding new place: country={}, city={}, lat={}, lng={}", country, city, lat, lng);

        PlaceTb tb = new PlaceTb();
        tb.setCountry(validateCountryCode(country));
        tb.setCounty(county);
        tb.setCity(city);
        tb.setBuilding(building);
        tb.setAddress(address);
        tb.setPostCode(postCode);
        tb.setLat(lat);
        tb.setLng(lng);

        if (lat != 0 && lng != 0) {
            Point point = createPoint(lng, lat);
            tb.setGeom(point);
            log.debug("Created geometry point: SRID={}, coordinates=({}, {})", SRID, lng, lat);
        }

        tb.setNotes(notes);
        tb.setProps(JsonValueUtils.getInstance().encodeAsJson(props));


        placeRepository.save(tb);
        log.info("Place created with id: {}", tb.getId());

        return build(tb, context);
    }

    private String validateCountryCode(String country) {
        if (country == null || country.isEmpty()) {
            throw new IllegalArgumentException("Country code is required");
        }

        String countryCode = country.toUpperCase();
        if (countryCode.length() != 2) {
            log.warn("Country code '{}' is not ISO 3166-1 alpha-2 format. Expected 2 characters.", country);
        }
        return countryCode;
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

    public Point createPointFromCoordinates(double longitude, double latitude) {
        return createPoint(longitude, latitude);
    }

    public List<PlaceRecord> findPlacesWithinRadius(double longitude, double latitude, double radiusMeters, AppSecurityContext context) {
        log.debug("Finding places within {} meters of coordinates ({}, {})", radiusMeters, longitude, latitude);

        Point centerPoint = createPoint(longitude, latitude);
        return placeRepository.findPlacesWithinRadius(centerPoint, radiusMeters)
                .stream()
                .map(tb -> build(tb, context))
                .toList();
    }

    public List<PlaceRecord> findPlacesWithinBoundingBox(double minLon, double minLat, double maxLon, double maxLat, AppSecurityContext context) {
        log.debug("Finding places within bounding box: ({}, {}) to ({}, {})", minLon, minLat, maxLon, maxLat);

        return placeRepository.findPlacesWithinBoundingBox(minLon, minLat, maxLon, maxLat)
                .stream()
                .map(tb -> build(tb, context))
                .toList();
    }

    public List<PlaceRecord> findNearestPlaces(double longitude, double latitude, AppSecurityContext context) {
        log.debug("Finding nearest places to coordinates ({}, {})", longitude, latitude);

        Point point = createPoint(longitude, latitude);
        return placeRepository.findNearestPlaces(point)
                .stream()
                .map(tb -> build(tb, context))
                .toList();
    }

    public List<PlaceRecord> findByCountryAndCityWithGeometry(String country, String city, AppSecurityContext context) {
        log.debug("Finding places in {} {} with geometry data", country, city);

        String normalizedCountry = validateCountryCode(country);
        return placeRepository.findByCountryAndCityWithGeometry(normalizedCountry, city)
                .stream()
                .map(tb -> build(tb, context))
                .toList();
    }

    public Optional<Double> calculateDistanceBetweenPlaces(Integer placeId1, Integer placeId2) {
        log.debug("Calculating distance between places {} and {}", placeId1, placeId2);

        if (placeId1 == null || placeId2 == null) {
            log.warn("Cannot calculate distance: place IDs cannot be null");
            return Optional.empty();
        }

        return Optional.ofNullable(placeRepository.calculateDistanceBetweenPlaces(placeId1, placeId2));
    }

    private PlaceRecord build(PlaceTb tb, AppSecurityContext context) {
        return new PlaceRecord(tb, placeRepository::save);
    }
}
