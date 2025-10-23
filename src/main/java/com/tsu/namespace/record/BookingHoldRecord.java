package com.tsu.namespace.record;

import com.tsu.auth.security.AppSecurityContext;
import com.tsu.namespace.api.BookingHold;
import com.tsu.namespace.entities.BookingHoldTb;
import com.tsu.namespace.val.BookingHoldVal;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Record wrapper for BookingHoldTb entity implementing BookingHold interface.
 */
@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class BookingHoldRecord implements BookingHold {

    @ToString.Include
    private final BookingHoldTb tb;
    private final AppSecurityContext context;

    public BookingHoldVal getValue() {
        return new BookingHoldVal(
                tb.getId().getNamespaceId(),
                tb.getId().getId(),
                tb.getResourceId(),
                tb.getUserId(),
                tb.getStartAt(),
                tb.getEndAt(),
                tb.getExpiresAt(),
                tb.getCreatedAt()
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
    public LocalDateTime getStartAt() {
        return tb.getStartAt();
    }

    @Override
    public LocalDateTime getEndAt() {
        return tb.getEndAt();
    }

    @Override
    public LocalDateTime getExpiresAt() {
        return tb.getExpiresAt();
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return tb.getCreatedAt();
    }

    // Setters

    public void setStartAt(LocalDateTime startAt) {
        tb.setStartAt(startAt);
    }

    public void setEndAt(LocalDateTime endAt) {
        tb.setEndAt(endAt);
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        tb.setExpiresAt(expiresAt);
    }
}
