package com.tsu.namespace.repo;

import com.tsu.namespace.entities.AuditLogTb;
import com.tsu.namespace.entities.id.AuditLogId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogTb, AuditLogId> {

    /**
     * Find a specific audit log entry by namespace and ID
     */
    Optional<AuditLogTb> findByIdNamespaceIdAndIdId(UUID namespaceId, UUID id);

    /**
     * Find all audit logs for a specific entity
     */
    @Query(value = """
            SELECT al.*
            FROM audit_log al
            WHERE al.namespace_id = ?1
              AND al.entity = ?2
              AND al.entity_id = ?3
            ORDER BY al.at DESC
            """, nativeQuery = true)
    Stream<AuditLogTb> findByNamespaceIdAndEntityAndEntityId(UUID namespaceId, String entity, UUID entityId);

    /**
     * Find audit logs by actor
     */
    @Query(value = """
            SELECT al.*
            FROM audit_log al
            WHERE al.namespace_id = ?1
              AND al.actor_id = ?2
            ORDER BY al.at DESC
            """, nativeQuery = true)
    Stream<AuditLogTb> findByNamespaceIdAndActorId(UUID namespaceId, UUID actorId);

    /**
     * Find audit logs by entity type
     */
    @Query(value = """
            SELECT al.*
            FROM audit_log al
            WHERE al.namespace_id = ?1
              AND al.entity = ?2
            ORDER BY al.at DESC
            """, nativeQuery = true)
    Stream<AuditLogTb> findByNamespaceIdAndEntity(UUID namespaceId, String entity);

    /**
     * Find audit logs by action
     */
    @Query(value = """
            SELECT al.*
            FROM audit_log al
            WHERE al.namespace_id = ?1
              AND al.action = ?2
            ORDER BY al.at DESC
            """, nativeQuery = true)
    Stream<AuditLogTb> findByNamespaceIdAndAction(UUID namespaceId, String action);

    /**
     * Find audit logs in a time range
     */
    @Query(value = """
            SELECT al.*
            FROM audit_log al
            WHERE al.namespace_id = ?1
              AND al.at >= ?2
              AND al.at <= ?3
            ORDER BY al.at DESC
            """, nativeQuery = true)
    Stream<AuditLogTb> findByNamespaceIdAndTimestampRange(UUID namespaceId, LocalDateTime start, LocalDateTime end);

    /**
     * Find recent audit logs for a namespace
     */
    @Query(value = """
            SELECT al.*
            FROM audit_log al
            WHERE al.namespace_id = ?1
            ORDER BY al.at DESC
            LIMIT ?2
            """, nativeQuery = true)
    Stream<AuditLogTb> findRecentByNamespaceId(UUID namespaceId, int limit);

    /**
     * Find audit logs for a specific entity and action
     */
    @Query(value = """
            SELECT al.*
            FROM audit_log al
            WHERE al.namespace_id = ?1
              AND al.entity = ?2
              AND al.entity_id = ?3
              AND al.action = ?4
            ORDER BY al.at DESC
            """, nativeQuery = true)
    Stream<AuditLogTb> findByNamespaceIdAndEntityAndEntityIdAndAction(UUID namespaceId, String entity, UUID entityId, String action);
}
