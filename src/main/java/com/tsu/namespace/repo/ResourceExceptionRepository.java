package com.tsu.namespace.repo;

import com.tsu.namespace.entities.ResourceExceptionTb;
import com.tsu.namespace.entities.id.ResourceExceptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface ResourceExceptionRepository extends JpaRepository<ResourceExceptionTb, ResourceExceptionId> {

    /**
     * Find all exceptions for a specific resource
     */
    @Query(value = """
            SELECT re.*
            FROM resource_exception re
            WHERE re.namespace_id = ?1 AND re.resource_id = ?2
            ORDER BY re.start_at
            """, nativeQuery = true)
    Stream<ResourceExceptionTb> findByNamespaceIdAndResourceIdOrderByStartAt(UUID namespaceId, UUID resourceId);

    /**
     * Find a specific exception by ID
     */
    Optional<ResourceExceptionTb> findByIdNamespaceIdAndIdId(UUID namespaceId, UUID id);

    /**
     * Find exceptions that overlap with a given time range
     */
    @Query(value = """
            SELECT re.*
            FROM resource_exception re
            WHERE re.namespace_id = ?1
              AND re.resource_id = ?2
              AND re.span && tstzrange(?3, ?4, '[)')
            ORDER BY re.start_at
            """, nativeQuery = true)
    Stream<ResourceExceptionTb> findOverlappingExceptions(UUID namespaceId, UUID resourceId, LocalDateTime start, LocalDateTime end);

    /**
     * Find active exceptions (current or future)
     */
    @Query(value = """
            SELECT re.*
            FROM resource_exception re
            WHERE re.namespace_id = ?1
              AND re.resource_id = ?2
              AND re.end_at > ?3
            ORDER BY re.start_at
            """, nativeQuery = true)
    Stream<ResourceExceptionTb> findActiveExceptions(UUID namespaceId, UUID resourceId, LocalDateTime currentTime);
}
