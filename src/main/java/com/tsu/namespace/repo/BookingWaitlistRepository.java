package com.tsu.namespace.repo;

import com.tsu.namespace.entities.BookingWaitlistTb;
import com.tsu.namespace.entities.id.BookingWaitlistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface BookingWaitlistRepository extends JpaRepository<BookingWaitlistTb, BookingWaitlistId> {

    /**
     * Find a specific waitlist entry by namespace and ID
     */
    Optional<BookingWaitlistTb> findByIdNamespaceIdAndIdId(UUID namespaceId, UUID id);

    /**
     * Find all waitlist entries for a specific resource ordered by priority
     */
    @Query(value = """
            SELECT bw.*
            FROM booking_waitlist bw
            WHERE bw.namespace_id = ?1
              AND bw.resource_id = ?2
            ORDER BY bw.priority ASC, bw.created_at ASC
            """, nativeQuery = true)
    Stream<BookingWaitlistTb> findByNamespaceIdAndResourceIdOrderByPriorityAndCreatedAt(UUID namespaceId, UUID resourceId);

    /**
     * Find all waitlist entries for a specific user
     */
    @Query(value = """
            SELECT bw.*
            FROM booking_waitlist bw
            WHERE bw.namespace_id = ?1
              AND bw.user_id = ?2
            ORDER BY bw.created_at DESC
            """, nativeQuery = true)
    Stream<BookingWaitlistTb> findByNamespaceIdAndUserId(UUID namespaceId, UUID userId);

    /**
     * Find waitlist entries that overlap with a given time range
     */
    @Query(value = """
            SELECT bw.*
            FROM booking_waitlist bw
            WHERE bw.namespace_id = ?1
              AND bw.resource_id = ?2
              AND (bw.desired_start, bw.desired_end) OVERLAPS (?3, ?4)
            ORDER BY bw.priority ASC, bw.created_at ASC
            """, nativeQuery = true)
    Stream<BookingWaitlistTb> findOverlappingWaitlistEntries(UUID namespaceId, UUID resourceId, LocalDateTime start, LocalDateTime end);

    /**
     * Find the highest priority waitlist entry for a resource and time range
     */
    @Query(value = """
            SELECT bw.*
            FROM booking_waitlist bw
            WHERE bw.namespace_id = ?1
              AND bw.resource_id = ?2
              AND (bw.desired_start, bw.desired_end) OVERLAPS (?3, ?4)
            ORDER BY bw.priority ASC, bw.created_at ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<BookingWaitlistTb> findTopPriorityWaitlistEntry(UUID namespaceId, UUID resourceId, LocalDateTime start, LocalDateTime end);

    /**
     * Find waitlist entries by priority
     */
    @Query(value = """
            SELECT bw.*
            FROM booking_waitlist bw
            WHERE bw.namespace_id = ?1
              AND bw.priority = ?2
            ORDER BY bw.created_at ASC
            """, nativeQuery = true)
    Stream<BookingWaitlistTb> findByNamespaceIdAndPriority(UUID namespaceId, Integer priority);
}
