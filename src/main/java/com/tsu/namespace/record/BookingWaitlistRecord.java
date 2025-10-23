package com.tsu.namespace.record;

import com.tsu.auth.security.AppSecurityContext;
import com.tsu.namespace.api.BookingWaitlist;
import com.tsu.namespace.entities.BookingWaitlistTb;
import com.tsu.namespace.val.BookingWaitlistVal;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Record wrapper for BookingWaitlistTb entity implementing BookingWaitlist interface.
 */
@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class BookingWaitlistRecord implements BookingWaitlist {

    @ToString.Include
    private final BookingWaitlistTb tb;
    private final AppSecurityContext context;

    public BookingWaitlistVal getValue() {
        return new BookingWaitlistVal(
                tb.getId().getNamespaceId(),
                tb.getId().getId(),
                tb.getResourceId(),
                tb.getUserId(),
                tb.getDesiredStart(),
                tb.getDesiredEnd(),
                tb.getPriority(),
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
    public LocalDateTime getDesiredStart() {
        return tb.getDesiredStart();
    }

    @Override
    public LocalDateTime getDesiredEnd() {
        return tb.getDesiredEnd();
    }

    @Override
    public Integer getPriority() {
        return tb.getPriority();
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return tb.getCreatedAt();
    }

    // Setters

    public void setDesiredStart(LocalDateTime desiredStart) {
        tb.setDesiredStart(desiredStart);
    }

    public void setDesiredEnd(LocalDateTime desiredEnd) {
        tb.setDesiredEnd(desiredEnd);
    }

    public void setPriority(Integer priority) {
        tb.setPriority(priority);
    }
}
