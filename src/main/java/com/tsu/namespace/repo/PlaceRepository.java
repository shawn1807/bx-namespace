package com.tsu.namespace.repo;

import com.tsu.namespace.entities.PlaceTb;
import com.tsu.namespace.entities.id.PlaceId;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<PlaceTb, PlaceId> {

    // Find places within a given radius (in meters) from a point using PostGIS geography type for accurate distance
    @Query(value = "SELECT * FROM place p WHERE ST_DWithin(p.geom::geography, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radiusMeters)", nativeQuery = true)
    List<PlaceTb> findPlacesWithinRadius(@Param("lat") double lat, @Param("lng") double lng, @Param("radiusMeters") double radiusMeters);

    // Overloaded method accepting Point object
    default List<PlaceTb> findPlacesWithinRadius(Point point, double radiusMeters) {
        return findPlacesWithinRadius(point.getY(), point.getX(), radiusMeters);
    }

    // Find places within a bounding box
    @Query(value = "SELECT * FROM place p WHERE ST_Within(p.geom, ST_MakeEnvelope(:minX, :minY, :maxX, :maxY, 4326))", nativeQuery = true)
    List<PlaceTb> findPlacesWithinBoundingBox(@Param("minX") double minX, @Param("minY") double minY,
                                              @Param("maxX") double maxX, @Param("maxY") double maxY);

    // Find nearest places ordered by distance
    @Query(value = "SELECT * FROM place p WHERE p.geom IS NOT NULL ORDER BY ST_Distance(p.geom::geography, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography)", nativeQuery = true)
    List<PlaceTb> findNearestPlaces(@Param("lat") double lat, @Param("lng") double lng);

    // Overloaded method accepting Point object
    default List<PlaceTb> findNearestPlaces(Point point) {
        return findNearestPlaces(point.getY(), point.getX());
    }

    // Find places by country and city with geometry
    @Query("SELECT p FROM PlaceTb p WHERE p.country = :country AND p.city = :city AND p.geom IS NOT NULL")
    List<PlaceTb> findByCountryAndCityWithGeometry(@Param("country") String country, @Param("city") String city);

    // Calculate distance between two places in meters
    @Query(value = "SELECT ST_Distance(p1.geom::geography, p2.geom::geography) FROM place p1, place p2 WHERE p1.id = :id1 AND p2.id = :id2", nativeQuery = true)
    Double calculateDistanceBetweenPlaces(@Param("id1") Integer id1, @Param("id2") Integer id2);

}
