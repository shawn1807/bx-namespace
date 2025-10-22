package com.tsu.namespace.service.impl;

import com.tsu.base.api.Place;
import com.tsu.namespace.helper.PlaceDbHelper;
import com.tsu.namespace.record.PlaceRecord;
import com.tsu.common.api.PlaceService;
import com.tsu.security.AppSecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService {

    private final PlaceDbHelper placeDbHelper;

    @Override
    public Optional<Place> findPlace(Integer id) {
        log.debug("Finding place by ID: {}", id);

        if (id == null) {
            log.warn("Place ID cannot be null");
            return Optional.empty();
        }

        // TODO: Need to pass AppSecurityContext - this service needs refactoring for multi-tenant support
        // For now, this won't work as PlaceDbHelper requires AppSecurityContext
        log.warn("Place lookup requires security context - service needs refactoring for multi-tenant support");
        return Optional.empty();
    }

    public Optional<PlaceRecord> findPlaceById(Integer id, AppSecurityContext context) {
        log.debug("Finding place by ID: {} for user: {}", id, context.getPrincipal().id());

        if (id == null) {
            log.warn("Place ID cannot be null");
            return Optional.empty();
        }

        return placeDbHelper.findPlaceById(id, context);
    }

    public PlaceRecord addPlace(String country, String county, String city, String building,
                               String address, String postCode, double lat, double lng,
                               String notes, Object props, AppSecurityContext context) {
        log.info("Creating new place: {} {} {}", country, city, address);

        return placeDbHelper.addPlace(country, county, city, building, address, postCode,
                                     lat, lng, notes, props, context);
    }

    // Spatial query methods

    public List<PlaceRecord> findPlacesWithinRadius(double longitude, double latitude, double radiusMeters, AppSecurityContext context) {
        log.info("Finding places within {} meters of ({}, {})", radiusMeters, longitude, latitude);

        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }

        return placeDbHelper.findPlacesWithinRadius(longitude, latitude, radiusMeters, context);
    }

    public List<PlaceRecord> findPlacesWithinBoundingBox(double minLon, double minLat, double maxLon, double maxLat, AppSecurityContext context) {
        log.info("Finding places within bounding box: ({}, {}) to ({}, {})", minLon, minLat, maxLon, maxLat);

        if (minLon >= maxLon || minLat >= maxLat) {
            throw new IllegalArgumentException("Invalid bounding box coordinates");
        }

        return placeDbHelper.findPlacesWithinBoundingBox(minLon, minLat, maxLon, maxLat, context);
    }

    public List<PlaceRecord> findNearestPlaces(double longitude, double latitude, AppSecurityContext context) {
        log.info("Finding nearest places to ({}, {})", longitude, latitude);

        return placeDbHelper.findNearestPlaces(longitude, latitude, context);
    }

    public List<PlaceRecord> findPlacesInCity(String countryCode, String city, AppSecurityContext context) {
        log.info("Finding places with geometry in {} {}", countryCode, city);

        if (countryCode == null || city == null) {
            throw new IllegalArgumentException("Country code and city cannot be null");
        }

        return placeDbHelper.findByCountryAndCityWithGeometry(countryCode, city, context);
    }

    public Optional<Double> calculateDistanceBetweenPlaces(Integer placeId1, Integer placeId2) {
        log.info("Calculating distance between places {} and {}", placeId1, placeId2);

        return placeDbHelper.calculateDistanceBetweenPlaces(placeId1, placeId2);
    }

    // Convenience methods with common radius values

    public List<PlaceRecord> findPlacesWithin1Km(double longitude, double latitude, AppSecurityContext context) {
        return findPlacesWithinRadius(longitude, latitude, 1000.0, context); // 1km
    }

    public List<PlaceRecord> findPlacesWithin5Km(double longitude, double latitude, AppSecurityContext context) {
        return findPlacesWithinRadius(longitude, latitude, 5000.0, context); // 5km
    }

    public List<PlaceRecord> findPlacesWithin10Km(double longitude, double latitude, AppSecurityContext context) {
        return findPlacesWithinRadius(longitude, latitude, 10000.0, context); // 10km
    }
}
