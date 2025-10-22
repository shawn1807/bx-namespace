package com.tsu.namespace.repo;

import com.tsu.namespace.entities.NamespaceTb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface NamespaceRepository extends JpaRepository<NamespaceTb, UUID> {

    Optional<NamespaceTb> findByName(String name);

    Optional<NamespaceTb> findByUri(String uri);

    @Query(value = """
            SELECT n.*
            FROM namespace n
            LEFT JOIN namespace_user nu ON nu.namespace_id = n.id
            WHERE
                 nu.principal_id = ?1
            ORDER BY n.modified_date desc
            """, nativeQuery = true)
    Stream<NamespaceTb> findByPrincipalId(UUID principalId);

    Stream<NamespaceTb> findByOwnerId(UUID principalId);

    @Query(value = """
            SELECT n.*
            FROM namespace n
            JOIN namespace_user nu ON nu.namespace_id = n.id
            WHERE
                 n.id = ?1 AND nu.user_id = ?2
            """, nativeQuery = true)
    Optional<NamespaceTb> findByNamespaceIdAndPrincipalId(UUID namespaceId, UUID principalId);
}
