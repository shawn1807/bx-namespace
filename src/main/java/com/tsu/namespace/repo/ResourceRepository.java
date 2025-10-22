package com.tsu.namespace.repo;

import com.tsu.namespace.api.ResourceType;
import com.tsu.namespace.entities.ResourceTb;
import com.tsu.namespace.entities.id.ResourceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface ResourceRepository extends JpaRepository<ResourceTb, ResourceId> {

    /**
     * Find all resources in a namespace
     */
    Stream<ResourceTb> findByIdNamespaceId(UUID namespaceId);

    /**
     * Find a specific resource by namespace and ID
     */
    Optional<ResourceTb> findByIdNamespaceIdAndIdId(UUID namespaceId, UUID id);

    /**
     * Find resources by type
     */
    Stream<ResourceTb> findByIdNamespaceIdAndType(UUID namespaceId, ResourceType type);

    /**
     * Find resources by name
     */
    Optional<ResourceTb> findByIdNamespaceIdAndName(UUID namespaceId, String name);

    /**
     * Find active resources in a namespace
     */
    Stream<ResourceTb> findByIdNamespaceIdAndActiveTrue(UUID namespaceId);

    /**
     * Find active resources by type
     */
    Stream<ResourceTb> findByIdNamespaceIdAndTypeAndActiveTrue(UUID namespaceId, ResourceType type);

    /**
     * Find resources by location
     */
    @Query(value = """
            SELECT r.*
            FROM resource r
            WHERE r.namespace_id = ?1 AND r.location = ?2
            """, nativeQuery = true)
    Stream<ResourceTb> findByNamespaceIdAndLocation(UUID namespaceId, String location);
}
