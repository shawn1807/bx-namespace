package com.tsu.namespace.repo;

import com.tsu.namespace.entities.PlaceAltNameTb;
import com.tsu.namespace.entities.id.PlaceAltNameId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for place alternate names.
 */
@Repository
public interface PlaceAltNameRepository extends JpaRepository<PlaceAltNameTb, PlaceAltNameId> {

    /**
     * Find all alternate names for a place.
     */
    List<PlaceAltNameTb> findByNamespaceIdAndPlaceId(UUID namespaceId, UUID placeId);

    /**
     * Find alternate names by language.
     */
    List<PlaceAltNameTb> findByNamespaceIdAndPlaceIdAndLang(UUID namespaceId, UUID placeId, String lang);

    /**
     * Search alternate names by prefix.
     */
    @Query("SELECT a FROM PlaceAltNameTb a WHERE a.namespaceId = :namespaceId " +
           "AND LOWER(a.name) LIKE LOWER(CONCAT(:prefix, '%'))")
    List<PlaceAltNameTb> searchByPrefix(@Param("namespaceId") UUID namespaceId,
                                        @Param("prefix") String prefix);

    /**
     * Fuzzy search across alternate names.
     */
    @Query(value = "SELECT a.* FROM place_alt_name a " +
                   "WHERE a.namespace_id = :namespaceId " +
                   "AND (a.name % :query OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
                   "ORDER BY similarity(a.name, :query) DESC " +
                   "LIMIT :limit",
           nativeQuery = true)
    List<PlaceAltNameTb> searchFuzzy(@Param("namespaceId") UUID namespaceId,
                                     @Param("query") String query,
                                     @Param("limit") int limit);

    /**
     * Delete all alternate names for a place.
     */
    void deleteByNamespaceIdAndPlaceId(UUID namespaceId, UUID placeId);
}
