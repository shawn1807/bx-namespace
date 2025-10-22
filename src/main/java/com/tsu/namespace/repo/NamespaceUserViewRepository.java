package com.tsu.namespace.repo;

import com.tsu.namespace.api.NamespaceUserType;
import com.tsu.namespace.entities.NamespaceUserViewTb;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Repository for querying the namespace_user_mv materialized view
 * This view provides efficient queries by denormalizing namespace_user, user_base, and namespace data
 */
@Repository
public interface NamespaceUserViewRepository extends JpaRepository<NamespaceUserViewTb, Integer>,
                                                      JpaSpecificationExecutor<NamespaceUserViewTb> {

    /**
     * Find a namespace user by namespace ID and user ID
     */
    Optional<NamespaceUserViewTb> findByNamespaceIdAndId(UUID namespaceId, Integer id);

    /**
     * Find a namespace user by namespace ID and principal ID
     */
    Optional<NamespaceUserViewTb> findByNamespaceIdAndPrincipalId(UUID namespaceId, UUID principalId);

    /**
     * Find all namespace users by namespace ID
     */
    Stream<NamespaceUserViewTb> findByNamespaceId(UUID namespaceId);

    /**
     * Find all namespace users by namespace ID with pagination
     */
    Page<NamespaceUserViewTb> findByNamespaceId(UUID namespaceId, Pageable pageable);

    /**
     * Find all namespace users by namespace ID and type
     */
    Stream<NamespaceUserViewTb> findByNamespaceIdAndType(UUID namespaceId, NamespaceUserType type);

    /**
     * Find all namespace users by namespace ID and type with pagination
     */
    Page<NamespaceUserViewTb> findByNamespaceIdAndType(UUID namespaceId, NamespaceUserType type, Pageable pageable);

    /**
     * Find all active namespace users by namespace ID
     */
    Stream<NamespaceUserViewTb> findByNamespaceIdAndActive(UUID namespaceId, boolean active);

    /**
     * Find all active namespace users by namespace ID with pagination
     */
    Page<NamespaceUserViewTb> findByNamespaceIdAndActive(UUID namespaceId, boolean active, Pageable pageable);

    /**
     * Find namespace users by email (partial match, case-insensitive)
     */
    Stream<NamespaceUserViewTb> findByNamespaceIdAndEmailContainingIgnoreCase(UUID namespaceId, String email);

    /**
     * Find namespace users by display name (partial match, case-insensitive)
     */
    Stream<NamespaceUserViewTb> findByNamespaceIdAndDisplayNameContainingIgnoreCase(UUID namespaceId, String displayName);
}
