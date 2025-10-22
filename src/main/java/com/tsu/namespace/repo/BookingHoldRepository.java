package com.tsu.namespace.repo;

import com.tsu.namespace.entities.BookingHoldTb;
import com.tsu.namespace.entities.id.BookingHoldId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface BookingHoldRepository extends JpaRepository<BookingHoldTb, BookingHoldId> {

    /**
     * Find a specific hold by namespace and ID
     */
    Optional<BookingHoldTb> findByIdNamespaceIdAndIdId(UUID namespaceId, UUID id);

    /**
     * Find all holds for a specific resource
     */
    @Query(value = """
            SELECT bh.*
            FROM booking_hold bh
            WHERE bh.namespace_id = ?1
              AND bh.resource_id = ?2
              AND bh.expires_at > ?3
            ORDER BY bh.created_at
            """, nativeQuery = true)
    Stream<BookingHoldTb> findActiveHoldsByResource(UUID namespaceId, UUID resourceId, LocalDateTime currentTime);

    /**
     * Find all holds for a specific user
     */
    @Query(value = """
            SELECT bh.*
            FROM booking_hold bh
            WHERE bh.namespace_id = ?1
              AND bh.user_id = ?2
              AND bh.expires_at > ?3
            ORDER BY bh.created_at
            """, nativeQuery = true)
    Stream<BookingHoldTb> findActiveHoldsByUser(UUID namespaceId, UUID userId, LocalDateTime currentTime);

    /**
     * Find holds that overlap with a given time range
     */
    @Query(value = """
            SELECT bh.*
            FROM booking_hold bh
            WHERE bh.namespace_id = ?1
              AND bh.resource_id = ?2
              AND bh.slot && tstzrange(?3, ?4, '[)')
              AND bh.expires_at > ?5
            ORDER BY bh.created_at
            """, nativeQuery = true)
    Stream<BookingHoldTb> findOverlappingHolds(UUID namespaceId, UUID resourceId, LocalDateTime start, LocalDateTime end, LocalDateTime currentTime);

    /**
     * Find expired holds for cleanup
     */
    @Query(value = """
            SELECT bh.*
            FROM booking_hold bh
            WHERE bh.namespace_id = ?1
              AND bh.expires_at <= ?2
            """, nativeQuery = true)
    Stream<BookingHoldTb> findExpiredHolds(UUID namespaceId, LocalDateTime currentTime);

    /**
     * Delete expired holds (cleanup operation)
     */
    @Modifying
    @Query(value = """
            DELETE FROM booking_hold bh
            WHERE bh.namespace_id = ?1
              AND bh.expires_at <= ?2
            """, nativeQuery = true)
    int deleteExpiredHolds(UUID namespaceId, LocalDateTime currentTime);

    /**
     * Check if a time slot is held
     */
    @Query(value = """
            SELECT COUNT(*) > 0
            FROM booking_hold bh
            WHERE bh.namespace_id = ?1
              AND bh.resource_id = ?2
              AND bh.slot && tstzrange(?3, ?4, '[)')
              AND bh.expires_at > ?5
            """, nativeQuery = true)
    boolean isSlotHeld(UUID namespaceId, UUID resourceId, LocalDateTime start, LocalDateTime end, LocalDateTime currentTime);
}
