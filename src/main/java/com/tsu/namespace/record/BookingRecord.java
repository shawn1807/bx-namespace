package com.tsu.namespace.record;

import com.tsu.auth.security.AppSecurityContext;
import com.tsu.namespace.api.Booking;
import com.tsu.namespace.api.BookingStatus;
import com.tsu.namespace.entities.BookingTb;
import com.tsu.namespace.val.BookingVal;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Record wrapper for BookingTb entity implementing Booking interface.
 */
@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class BookingRecord implements Booking {

    @ToString.Include
    private final BookingTb tb;
    private final AppSecurityContext context;

    public BookingVal getValue() {
        return new BookingVal(
                tb.getId().getNamespaceId(),
                tb.getId().getId(),
                tb.getResourceId(),
                tb.getUserId(),
                tb.getStatus(),
                tb.getStartAt(),
                tb.getEndAt(),
                tb.getTitle(),
                tb.getNotes(),
                tb.getCreatedAt(),
                tb.getCreatedBy(),
                tb.getUpdatedAt(),
                tb.getUpdatedBy(),
                tb.getDeletedAt()
        );
    }

    @Override
    public UUID getNamespaceId() {
        return tb.getId().getNamespaceId();
    }

    @Override
    public UUID getId() {
        return tb.getId().getId();
    }

    @Override
    public UUID getResourceId() {
        return tb.getResourceId();
    }

    @Override
    public UUID getUserId() {
        return tb.getUserId();
    }

    @Override
    public BookingStatus getStatus() {
        return tb.getStatus();
    }

    @Override
    public LocalDateTime getStartAt() {
        return tb.getStartAt();
    }

    @Override
    public LocalDateTime getEndAt() {
        return tb.getEndAt();
    }

    @Override
    public String getTitle() {
        return tb.getTitle();
    }

    @Override
    public String getNotes() {
        return tb.getNotes();
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return tb.getCreatedAt();
    }

    @Override
    public UUID getCreatedBy() {
        return tb.getCreatedBy();
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return tb.getUpdatedAt();
    }

    @Override
    public UUID getUpdatedBy() {
        return tb.getUpdatedBy();
    }

    @Override
    public LocalDateTime getDeletedAt() {
        return tb.getDeletedAt();
    }

    // Setters for mutable fields

    public void setStatus(BookingStatus status) {
        tb.setStatus(status);
    }

    public void setStartAt(LocalDateTime startAt) {
        tb.setStartAt(startAt);
    }

    public void setEndAt(LocalDateTime endAt) {
        tb.setEndAt(endAt);
    }

    public void setTitle(String title) {
        tb.setTitle(title);
    }

    public void setNotes(String notes) {
        tb.setNotes(notes);
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        tb.setDeletedAt(deletedAt);
    }
}
