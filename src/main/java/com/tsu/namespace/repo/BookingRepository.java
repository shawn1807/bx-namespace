package com.tsu.namespace.repo;

import com.tsu.namespace.entities.BookingTb;
import com.tsu.namespace.entities.id.BookingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface BookingRepository extends JpaRepository<BookingTb, BookingId>, JpaSpecificationExecutor<BookingTb> {

    /**
     * Find a specific booking by namespace and ID
     */
    Optional<BookingTb> findByIdNamespaceIdAndIdId(UUID namespaceId, UUID id);

    /**
     * Find all bookings for a specific resource
     */
    @Query(value = """
            SELECT b.*
            FROM booking b
            WHERE b.namespace_id = ?1
              AND b.resource_id = ?2
              AND b.deleted_at IS NULL
            ORDER BY b.start_at
            """, nativeQuery = true)
    Stream<BookingTb> findByNamespaceIdAndResourceId(UUID namespaceId, UUID resourceId);

    /**
     * Find all bookings for a specific user
     */
    @Query(value = """
            SELECT b.*
            FROM booking b
            WHERE b.namespace_id = ?1
              AND b.user_id = ?2
              AND b.deleted_at IS NULL
            ORDER BY b.start_at
            """, nativeQuery = true)
    Stream<BookingTb> findByNamespaceIdAndUserId(UUID namespaceId, UUID userId);

    /**
     * Find bookings by status
     */
    @Query(value = """
            SELECT b.*
            FROM booking b
            WHERE b.namespace_id = ?1
              AND b.status = ?2
              AND b.deleted_at IS NULL
            ORDER BY b.start_at
            """, nativeQuery = true)
    Stream<BookingTb> findByNamespaceIdAndStatus(UUID namespaceId, String status);

    /**
     * Find bookings that overlap with a given time range
     */
    @Query(value = """
            SELECT b.*
            FROM booking b
            WHERE b.namespace_id = ?1
              AND b.resource_id = ?2
              AND b.slot && tstzrange(?3, ?4, '[)')
              AND b.deleted_at IS NULL
              AND b.status IN ('CONFIRMED', 'TENTATIVE')
            ORDER BY b.start_at
            """, nativeQuery = true)
    Stream<BookingTb> findOverlappingBookings(UUID namespaceId, UUID resourceId, LocalDateTime start, LocalDateTime end);

    /**
     * Find upcoming bookings for a user
     */
    @Query(value = """
            SELECT b.*
            FROM booking b
            WHERE b.namespace_id = ?1
              AND b.user_id = ?2
              AND b.start_at > ?3
              AND b.deleted_at IS NULL
              AND b.status IN ('CONFIRMED', 'TENTATIVE')
            ORDER BY b.start_at
            """, nativeQuery = true)
    Stream<BookingTb> findUpcomingBookingsByUser(UUID namespaceId, UUID userId, LocalDateTime fromTime);

    /**
     * Find bookings in a time range for a resource
     */
    @Query(value = """
            SELECT b.*
            FROM booking b
            WHERE b.namespace_id = ?1
              AND b.resource_id = ?2
              AND b.start_at >= ?3
              AND b.end_at <= ?4
              AND b.deleted_at IS NULL
            ORDER BY b.start_at
            """, nativeQuery = true)
    Stream<BookingTb> findBookingsInRange(UUID namespaceId, UUID resourceId, LocalDateTime startRange, LocalDateTime endRange);

    /**
     * Check if a time slot is available (no overlapping bookings)
     */
    @Query(value = """
            SELECT COUNT(*) = 0
            FROM booking b
            WHERE b.namespace_id = ?1
              AND b.resource_id = ?2
              AND b.slot && tstzrange(?3, ?4, '[)')
              AND b.deleted_at IS NULL
              AND b.status IN ('CONFIRMED', 'TENTATIVE')
            """, nativeQuery = true)
    boolean isSlotAvailable(UUID namespaceId, UUID resourceId, LocalDateTime start, LocalDateTime end);
}
