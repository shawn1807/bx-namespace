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
public class BookingRecord{

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
                tb.getCreatedDate(),
                tb.getCreatedBy(),
                tb.getModifiedDate(),
                tb.getModifiedBy(),
                tb.getDeletedDate()
        );
    }

   
    public UUID getNamespaceId() {
        return tb.getId().getNamespaceId();
    }

   
    public UUID getId() {
        return tb.getId().getId();
    }

   
    public UUID getResourceId() {
        return tb.getResourceId();
    }

   
    public UUID getUserId() {
        return tb.getUserId();
    }

   
    public BookingStatus getStatus() {
        return tb.getStatus();
    }

   
    public LocalDateTime getStartAt() {
        return tb.getStartAt();
    }

   
    public LocalDateTime getEndAt() {
        return tb.getEndAt();
    }

   
    public String getTitle() {
        return tb.getTitle();
    }

   
    public String getNotes() {
        return tb.getNotes();
    }

   
    public LocalDateTime getCreatedDate() {
        return tb.getCreatedDate();
    }

   
    public UUID getCreatedBy() {
        return tb.getCreatedBy();
    }

   
    public LocalDateTime getModifiedDate() {
        return tb.getModifiedDate();
    }

   
    public UUID getModifiedBy() {
        return tb.getModifiedBy();
    }

   
    public LocalDateTime getDeletedAt() {
        return tb.getDeletedDate();
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
        tb.setDeletedDate(deletedAt);
    }
}
