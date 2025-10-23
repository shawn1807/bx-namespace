package com.tsu.namespace.api.manager;

import com.tsu.auth.permissions.BookingPermission;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.common.api.ActionPack;
import com.tsu.namespace.api.*;
import com.tsu.namespace.helper.BookingDbHelper;
import com.tsu.namespace.record.BookingHoldRecord;
import com.tsu.namespace.record.BookingRecord;
import com.tsu.namespace.record.BookingWaitlistRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of BookingManager for namespace-scoped booking management.
 */
@Slf4j
@RequiredArgsConstructor
public class BookingManagerImpl implements BookingManager {

    private final Namespace namespace;
    private final AppSecurityContext context;
    private final BookingDbHelper dbHelper;

    @Override
    public Booking createBooking(UUID resourceId, UUID userId, LocalDateTime startAt,
                                LocalDateTime endAt, String title, String notes, BookingStatus status) {
        log.debug("Creating booking: resourceId={}, userId={}, start={}, end={}",
                resourceId, userId, startAt, endAt);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.CREATE, Map.of(
                        "resourceId", resourceId,
                        "userId", userId,
                        "startAt", startAt,
                        "endAt", endAt
                ))
        );

        BookingRecord record = dbHelper.createBooking(
                namespace.getId(), resourceId, userId, startAt, endAt, title, notes, status, context
        );

        return record;
    }

    @Override
    public Optional<Booking> getBooking(UUID bookingId) {
        return dbHelper.findBookingById(namespace.getId(), bookingId, context)
                .map(r -> (Booking) r);
    }

    @Override
    public Page<Booking> getBookings(Pageable pageable) {
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.VIEW_ALL, Map.of())
        );

        return dbHelper.findAllBookings(namespace.getId(), pageable, context)
                .map(r -> (Booking) r);
    }

    @Override
    public Page<Booking> getBookingsByResource(UUID resourceId, Pageable pageable) {
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.VIEW_ALL, Map.of("resourceId", resourceId))
        );

        return dbHelper.findBookingsByResource(namespace.getId(), resourceId, pageable, context)
                .map(r -> (Booking) r);
    }

    @Override
    public Page<Booking> getBookingsByUser(UUID userId, Pageable pageable) {
        // Users can view their own bookings
        boolean isOwn = userId.equals(context.getUserId());
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(isOwn ? BookingPermission.VIEW_OWN : BookingPermission.VIEW_ALL,
                        Map.of("userId", userId))
        );

        return dbHelper.findBookingsByUser(namespace.getId(), userId, pageable, context)
                .map(r -> (Booking) r);
    }

    @Override
    public Page<Booking> getBookingsByStatus(BookingStatus status, Pageable pageable) {
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.VIEW_ALL, Map.of("status", status))
        );

        return dbHelper.findBookingsByStatus(namespace.getId(), status, pageable, context)
                .map(r -> (Booking) r);
    }

    @Override
    public List<Booking> getBookingsInRange(UUID resourceId, LocalDateTime startAt, LocalDateTime endAt) {
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.VIEW_ALL, Map.of("resourceId", resourceId))
        );

        return dbHelper.findBookingsInRange(namespace.getId(), resourceId, startAt, endAt, context)
                .stream()
                .map(r -> (Booking) r)
                .toList();
    }

    @Override
    public Page<Booking> getUpcomingBookings(UUID userId, LocalDateTime fromDate, Pageable pageable) {
        boolean isOwn = userId.equals(context.getUserId());
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(isOwn ? BookingPermission.VIEW_OWN : BookingPermission.VIEW_ALL,
                        Map.of("userId", userId))
        );

        return dbHelper.findUpcomingBookings(namespace.getId(), userId, fromDate, pageable, context)
                .map(r -> (Booking) r);
    }

    @Override
    public Booking updateBooking(UUID bookingId, LocalDateTime startAt, LocalDateTime endAt,
                                String title, String notes) {
        log.debug("Updating booking: {}", bookingId);

        // Check if user owns the booking
        Optional<BookingRecord> existing = dbHelper.findBookingById(namespace.getId(), bookingId, context);
        boolean isOwn = existing.map(b -> b.getUserId().equals(context.getUserId())).orElse(false);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(isOwn ? BookingPermission.EDIT_OWN : BookingPermission.EDIT_ANY,
                        Map.of("bookingId", bookingId))
        );

        BookingRecord record = dbHelper.updateBooking(
                namespace.getId(), bookingId, startAt, endAt, title, notes, context
        );

        return record;
    }

    @Override
    public Booking changeBookingStatus(UUID bookingId, BookingStatus status) {
        log.debug("Changing booking status: {} to {}", bookingId, status);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.EDIT_ANY, Map.of("bookingId", bookingId, "status", status))
        );

        BookingRecord record = dbHelper.changeBookingStatus(namespace.getId(), bookingId, status, context);
        return record;
    }

    @Override
    public Booking confirmBooking(UUID bookingId) {
        log.debug("Confirming booking: {}", bookingId);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.APPROVE, Map.of("bookingId", bookingId))
        );

        return dbHelper.changeBookingStatus(namespace.getId(), bookingId, BookingStatus.CONFIRMED, context);
    }

    @Override
    public Booking cancelBooking(UUID bookingId) {
        log.debug("Cancelling booking: {}", bookingId);

        Optional<BookingRecord> existing = dbHelper.findBookingById(namespace.getId(), bookingId, context);
        boolean isOwn = existing.map(b -> b.getUserId().equals(context.getUserId())).orElse(false);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(isOwn ? BookingPermission.CANCEL_OWN : BookingPermission.CANCEL_ANY,
                        Map.of("bookingId", bookingId))
        );

        return dbHelper.changeBookingStatus(namespace.getId(), bookingId, BookingStatus.CANCELLED, context);
    }

    @Override
    public Booking completeBooking(UUID bookingId) {
        log.debug("Completing booking: {}", bookingId);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.MARK_COMPLETED, Map.of("bookingId", bookingId))
        );

        return dbHelper.changeBookingStatus(namespace.getId(), bookingId, BookingStatus.COMPLETED, context);
    }

    @Override
    public Booking markNoShow(UUID bookingId) {
        log.debug("Marking booking as no-show: {}", bookingId);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.MARK_COMPLETED, Map.of("bookingId", bookingId))
        );

        return dbHelper.changeBookingStatus(namespace.getId(), bookingId, BookingStatus.NO_SHOW, context);
    }

    @Override
    public void deleteBooking(UUID bookingId) {
        log.debug("Soft deleting booking: {}", bookingId);

        Optional<BookingRecord> existing = dbHelper.findBookingById(namespace.getId(), bookingId, context);
        boolean isOwn = existing.map(b -> b.getUserId().equals(context.getUserId())).orElse(false);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(isOwn ? BookingPermission.CANCEL_OWN : BookingPermission.CANCEL_ANY,
                        Map.of("bookingId", bookingId))
        );

        dbHelper.softDeleteBooking(namespace.getId(), bookingId, context);
    }

    @Override
    public void permanentlyDeleteBooking(UUID bookingId) {
        log.debug("Permanently deleting booking: {}", bookingId);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.CANCEL_ANY, Map.of("bookingId", bookingId))
        );

        dbHelper.deleteBooking(namespace.getId(), bookingId);
    }

    @Override
    public BookingHold createHold(UUID resourceId, UUID userId, LocalDateTime startAt,
                                 LocalDateTime endAt, LocalDateTime expiresAt) {
        log.debug("Creating booking hold: resourceId={}, userId={}", resourceId, userId);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.CREATE, Map.of(
                        "resourceId", resourceId,
                        "userId", userId
                ))
        );

        BookingHoldRecord record = dbHelper.createHold(
                namespace.getId(), resourceId, userId, startAt, endAt, expiresAt, context
        );

        return record;
    }

    @Override
    public Optional<BookingHold> getHold(UUID holdId) {
        return dbHelper.findHoldById(namespace.getId(), holdId, context)
                .map(r -> (BookingHold) r);
    }

    @Override
    public List<BookingHold> getActiveHoldsByResource(UUID resourceId) {
        return dbHelper.findActiveHoldsByResource(namespace.getId(), resourceId, context)
                .stream()
                .map(r -> (BookingHold) r)
                .toList();
    }

    @Override
    public List<BookingHold> getActiveHoldsByUser(UUID userId) {
        return dbHelper.findActiveHoldsByUser(namespace.getId(), userId, context)
                .stream()
                .map(r -> (BookingHold) r)
                .toList();
    }

    @Override
    public void releaseHold(UUID holdId) {
        log.debug("Releasing booking hold: {}", holdId);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.CANCEL_OWN, Map.of("holdId", holdId))
        );

        dbHelper.deleteHold(namespace.getId(), holdId);
    }

    @Override
    public int cleanupExpiredHolds() {
        log.debug("Cleaning up expired holds");
        return dbHelper.cleanupExpiredHolds(namespace.getId());
    }

    @Override
    public BookingWaitlist addToWaitlist(UUID resourceId, UUID userId, LocalDateTime desiredStart,
                                        LocalDateTime desiredEnd, Integer priority) {
        log.debug("Adding to waitlist: resourceId={}, userId={}", resourceId, userId);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.CREATE, Map.of(
                        "resourceId", resourceId,
                        "userId", userId
                ))
        );

        BookingWaitlistRecord record = dbHelper.addToWaitlist(
                namespace.getId(), resourceId, userId, desiredStart, desiredEnd, priority, context
        );

        return record;
    }

    @Override
    public Optional<BookingWaitlist> getWaitlistEntry(UUID waitlistId) {
        return dbHelper.findWaitlistEntryById(namespace.getId(), waitlistId, context)
                .map(r -> (BookingWaitlist) r);
    }

    @Override
    public List<BookingWaitlist> getWaitlistByResource(UUID resourceId) {
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.VIEW_ALL, Map.of("resourceId", resourceId))
        );

        return dbHelper.findWaitlistByResource(namespace.getId(), resourceId, context)
                .stream()
                .map(r -> (BookingWaitlist) r)
                .toList();
    }

    @Override
    public List<BookingWaitlist> getWaitlistByUser(UUID userId) {
        boolean isOwn = userId.equals(context.getUserId());
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(isOwn ? BookingPermission.VIEW_OWN : BookingPermission.VIEW_ALL,
                        Map.of("userId", userId))
        );

        return dbHelper.findWaitlistByUser(namespace.getId(), userId, context)
                .stream()
                .map(r -> (BookingWaitlist) r)
                .toList();
    }

    @Override
    public Optional<BookingWaitlist> getTopPriorityWaitlistEntry(UUID resourceId) {
        return dbHelper.findTopPriorityWaitlistEntry(namespace.getId(), resourceId, context)
                .map(r -> (BookingWaitlist) r);
    }

    @Override
    public void removeFromWaitlist(UUID waitlistId) {
        log.debug("Removing from waitlist: {}", waitlistId);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.CANCEL_OWN, Map.of("waitlistId", waitlistId))
        );

        dbHelper.deleteWaitlistEntry(namespace.getId(), waitlistId);
    }

    @Override
    public boolean isSlotAvailable(UUID resourceId, LocalDateTime startAt, LocalDateTime endAt) {
        return dbHelper.isSlotAvailable(namespace.getId(), resourceId, startAt, endAt);
    }

    @Override
    public List<Booking> findOverlappingBookings(UUID resourceId, LocalDateTime startAt, LocalDateTime endAt) {
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(BookingPermission.VIEW_ALL, Map.of("resourceId", resourceId))
        );

        return dbHelper.findOverlappingBookings(namespace.getId(), resourceId, startAt, endAt, context)
                .stream()
                .map(r -> (Booking) r)
                .toList();
    }
}
