package com.tsu.namespace.helper;

import com.tsu.auth.security.AppSecurityContext;
import com.tsu.namespace.api.BookingStatus;
import com.tsu.namespace.entities.BookingHoldTb;
import com.tsu.namespace.entities.BookingTb;
import com.tsu.namespace.entities.BookingWaitlistTb;
import com.tsu.namespace.entities.id.BookingHoldId;
import com.tsu.namespace.entities.id.BookingId;
import com.tsu.namespace.entities.id.BookingWaitlistId;
import com.tsu.namespace.record.BookingHoldRecord;
import com.tsu.namespace.record.BookingRecord;
import com.tsu.namespace.record.BookingWaitlistRecord;
import com.tsu.namespace.repo.BookingHoldRepository;
import com.tsu.namespace.repo.BookingRepository;
import com.tsu.namespace.repo.BookingWaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Database helper for booking operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingDbHelper {

    private final BookingRepository bookingRepository;
    private final BookingHoldRepository holdRepository;
    private final BookingWaitlistRepository waitlistRepository;

    // Booking CRUD

    public BookingRecord createBooking(UUID namespaceId, UUID resourceId, UUID userId,
                                      LocalDateTime startAt, LocalDateTime endAt,
                                      String title, String notes, BookingStatus status,
                                      AppSecurityContext context) {
        log.debug("Creating booking: namespaceId={}, resourceId={}, userId={}, start={}, end={}",
                namespaceId, resourceId, userId, startAt, endAt);

        BookingTb tb = new BookingTb();
        BookingId id = new BookingId(namespaceId, UUID.randomUUID());
        tb.setId(id);
        tb.setResourceId(resourceId);
        tb.setUserId(userId);
        tb.setStatus(status);
        tb.setStartAt(startAt);
        tb.setEndAt(endAt);
        tb.setTitle(title);
        tb.setNotes(notes);
        tb.setCreatedAt(LocalDateTime.now());
        tb.setCreatedBy(context.getUserId());

        bookingRepository.save(tb);
        log.info("Booking created with id: {}", id.getId());

        return build(tb, context);
    }

    public Optional<BookingRecord> findBookingById(UUID namespaceId, UUID bookingId, AppSecurityContext context) {
        return bookingRepository.findByIdNamespaceIdAndIdId(namespaceId, bookingId)
                .map(tb -> build(tb, context));
    }

    public Page<BookingRecord> findAllBookings(UUID namespaceId, Pageable pageable, AppSecurityContext context) {
        return bookingRepository.findByIdNamespaceId(namespaceId, pageable)
                .map(tb -> build(tb, context));
    }

    public Page<BookingRecord> findBookingsByResource(UUID namespaceId, UUID resourceId,
                                                     Pageable pageable, AppSecurityContext context) {
        return bookingRepository.findByNamespaceIdAndResourceId(namespaceId, resourceId, pageable)
                .map(tb -> build(tb, context));
    }

    public Page<BookingRecord> findBookingsByUser(UUID namespaceId, UUID userId,
                                                 Pageable pageable, AppSecurityContext context) {
        return bookingRepository.findByNamespaceIdAndUserId(namespaceId, userId, pageable)
                .map(tb -> build(tb, context));
    }

    public Page<BookingRecord> findBookingsByStatus(UUID namespaceId, BookingStatus status,
                                                   Pageable pageable, AppSecurityContext context) {
        return bookingRepository.findByNamespaceIdAndStatus(namespaceId, status.name(), pageable)
                .map(tb -> build(tb, context));
    }

    public List<BookingRecord> findBookingsInRange(UUID namespaceId, UUID resourceId,
                                                   LocalDateTime startAt, LocalDateTime endAt,
                                                   AppSecurityContext context) {
        return bookingRepository.findBookingsInRange(namespaceId, resourceId, startAt, endAt)
                .stream()
                .map(tb -> build(tb, context))
                .toList();
    }

    public Page<BookingRecord> findUpcomingBookings(UUID namespaceId, UUID userId,
                                                   LocalDateTime fromDate, Pageable pageable,
                                                   AppSecurityContext context) {
        return bookingRepository.findUpcomingBookingsByUser(namespaceId, userId, fromDate, pageable)
                .map(tb -> build(tb, context));
    }

    public List<BookingRecord> findOverlappingBookings(UUID namespaceId, UUID resourceId,
                                                      LocalDateTime startAt, LocalDateTime endAt,
                                                      AppSecurityContext context) {
        return bookingRepository.findOverlappingBookings(namespaceId, resourceId, startAt, endAt)
                .stream()
                .map(tb -> build(tb, context))
                .toList();
    }

    public BookingRecord updateBooking(UUID namespaceId, UUID bookingId,
                                      LocalDateTime startAt, LocalDateTime endAt,
                                      String title, String notes, AppSecurityContext context) {
        BookingTb tb = bookingRepository.findByIdNamespaceIdAndIdId(namespaceId, bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (startAt != null) tb.setStartAt(startAt);
        if (endAt != null) tb.setEndAt(endAt);
        if (title != null) tb.setTitle(title);
        if (notes != null) tb.setNotes(notes);

        tb.setUpdatedAt(LocalDateTime.now());
        tb.setUpdatedBy(context.getUserId());

        bookingRepository.save(tb);
        log.info("Booking updated: {}", bookingId);

        return build(tb, context);
    }

    public BookingRecord changeBookingStatus(UUID namespaceId, UUID bookingId,
                                            BookingStatus status, AppSecurityContext context) {
        BookingTb tb = bookingRepository.findByIdNamespaceIdAndIdId(namespaceId, bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        tb.setStatus(status);
        tb.setUpdatedAt(LocalDateTime.now());
        tb.setUpdatedBy(context.getUserId());

        bookingRepository.save(tb);
        log.info("Booking {} status changed to {}", bookingId, status);

        return build(tb, context);
    }

    public void softDeleteBooking(UUID namespaceId, UUID bookingId, AppSecurityContext context) {
        BookingTb tb = bookingRepository.findByIdNamespaceIdAndIdId(namespaceId, bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        tb.setDeletedAt(LocalDateTime.now());
        bookingRepository.save(tb);
        log.info("Booking soft deleted: {}", bookingId);
    }

    public void deleteBooking(UUID namespaceId, UUID bookingId) {
        BookingId id = new BookingId(namespaceId, bookingId);
        bookingRepository.deleteById(id);
        log.info("Booking permanently deleted: {}", bookingId);
    }

    public boolean isSlotAvailable(UUID namespaceId, UUID resourceId,
                                  LocalDateTime startAt, LocalDateTime endAt) {
        return bookingRepository.isSlotAvailable(namespaceId, resourceId, startAt, endAt);
    }

    // Holds

    public BookingHoldRecord createHold(UUID namespaceId, UUID resourceId, UUID userId,
                                       LocalDateTime startAt, LocalDateTime endAt,
                                       LocalDateTime expiresAt, AppSecurityContext context) {
        log.debug("Creating booking hold: resourceId={}, userId={}, start={}, end={}, expires={}",
                resourceId, userId, startAt, endAt, expiresAt);

        BookingHoldTb tb = new BookingHoldTb();
        BookingHoldId id = new BookingHoldId(namespaceId, UUID.randomUUID());
        tb.setId(id);
        tb.setResourceId(resourceId);
        tb.setUserId(userId);
        tb.setStartAt(startAt);
        tb.setEndAt(endAt);
        tb.setExpiresAt(expiresAt);
        tb.setCreatedAt(LocalDateTime.now());

        holdRepository.save(tb);
        log.info("Booking hold created with id: {}", id.getId());

        return buildHold(tb, context);
    }

    public Optional<BookingHoldRecord> findHoldById(UUID namespaceId, UUID holdId, AppSecurityContext context) {
        BookingHoldId id = new BookingHoldId(namespaceId, holdId);
        return holdRepository.findById(id)
                .map(tb -> buildHold(tb, context));
    }

    public List<BookingHoldRecord> findActiveHoldsByResource(UUID namespaceId, UUID resourceId,
                                                            AppSecurityContext context) {
        LocalDateTime now = LocalDateTime.now();
        return holdRepository.findActiveHoldsByResource(namespaceId, resourceId, now)
                .stream()
                .map(tb -> buildHold(tb, context))
                .toList();
    }

    public List<BookingHoldRecord> findActiveHoldsByUser(UUID namespaceId, UUID userId,
                                                        AppSecurityContext context) {
        LocalDateTime now = LocalDateTime.now();
        return holdRepository.findActiveHoldsByUser(namespaceId, userId, now)
                .stream()
                .map(tb -> buildHold(tb, context))
                .toList();
    }

    public void deleteHold(UUID namespaceId, UUID holdId) {
        BookingHoldId id = new BookingHoldId(namespaceId, holdId);
        holdRepository.deleteById(id);
        log.info("Booking hold deleted: {}", holdId);
    }

    public int cleanupExpiredHolds(UUID namespaceId) {
        LocalDateTime now = LocalDateTime.now();
        int count = holdRepository.deleteExpiredHolds(namespaceId, now);
        log.info("Cleaned up {} expired holds in namespace {}", count, namespaceId);
        return count;
    }

    // Waitlist

    public BookingWaitlistRecord addToWaitlist(UUID namespaceId, UUID resourceId, UUID userId,
                                              LocalDateTime desiredStart, LocalDateTime desiredEnd,
                                              Integer priority, AppSecurityContext context) {
        log.debug("Adding to waitlist: resourceId={}, userId={}, start={}, end={}, priority={}",
                resourceId, userId, desiredStart, desiredEnd, priority);

        BookingWaitlistTb tb = new BookingWaitlistTb();
        BookingWaitlistId id = new BookingWaitlistId(namespaceId, UUID.randomUUID());
        tb.setId(id);
        tb.setResourceId(resourceId);
        tb.setUserId(userId);
        tb.setDesiredStart(desiredStart);
        tb.setDesiredEnd(desiredEnd);
        tb.setPriority(priority);
        tb.setCreatedAt(LocalDateTime.now());

        waitlistRepository.save(tb);
        log.info("Waitlist entry created with id: {}", id.getId());

        return buildWaitlist(tb, context);
    }

    public Optional<BookingWaitlistRecord> findWaitlistEntryById(UUID namespaceId, UUID waitlistId,
                                                                 AppSecurityContext context) {
        BookingWaitlistId id = new BookingWaitlistId(namespaceId, waitlistId);
        return waitlistRepository.findById(id)
                .map(tb -> buildWaitlist(tb, context));
    }

    public List<BookingWaitlistRecord> findWaitlistByResource(UUID namespaceId, UUID resourceId,
                                                             AppSecurityContext context) {
        return waitlistRepository.findByNamespaceIdAndResourceIdOrderByPriorityAscCreatedAtAsc(
                namespaceId, resourceId)
                .stream()
                .map(tb -> buildWaitlist(tb, context))
                .toList();
    }

    public List<BookingWaitlistRecord> findWaitlistByUser(UUID namespaceId, UUID userId,
                                                         AppSecurityContext context) {
        return waitlistRepository.findByNamespaceIdAndUserId(namespaceId, userId)
                .stream()
                .map(tb -> buildWaitlist(tb, context))
                .toList();
    }

    public Optional<BookingWaitlistRecord> findTopPriorityWaitlistEntry(UUID namespaceId, UUID resourceId,
                                                                        AppSecurityContext context) {
        return waitlistRepository.findTopPriorityWaitlistEntry(namespaceId, resourceId)
                .map(tb -> buildWaitlist(tb, context));
    }

    public void deleteWaitlistEntry(UUID namespaceId, UUID waitlistId) {
        BookingWaitlistId id = new BookingWaitlistId(namespaceId, waitlistId);
        waitlistRepository.deleteById(id);
        log.info("Waitlist entry deleted: {}", waitlistId);
    }

    // Record builders

    private BookingRecord build(BookingTb tb, AppSecurityContext context) {
        return new BookingRecord(tb, context);
    }

    private BookingHoldRecord buildHold(BookingHoldTb tb, AppSecurityContext context) {
        return new BookingHoldRecord(tb, context);
    }

    private BookingWaitlistRecord buildWaitlist(BookingWaitlistTb tb, AppSecurityContext context) {
        return new BookingWaitlistRecord(tb, context);
    }
}
