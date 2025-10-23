package com.tsu.namespace.record;

import com.tsu.auth.security.AppSecurityContext;
import com.tsu.namespace.api.ResourceException;
import com.tsu.namespace.entities.ResourceExceptionTb;
import com.tsu.namespace.val.ResourceExceptionVal;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Record wrapper for ResourceExceptionTb entity.
 */
@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class ResourceExceptionRecord implements ResourceException {

    @ToString.Include
    private final ResourceExceptionTb tb;
    private final AppSecurityContext context;

    public ResourceExceptionVal getValue() {
        return new ResourceExceptionVal(
                tb.getNamespaceId(),
                tb.getId(),
                tb.getResourceId(),
                tb.getStartAt(),
                tb.getEndAt(),
                tb.getReason()
        );
    }

    @Override
    public UUID getNamespaceId() {
        return tb.getNamespaceId();
    }

    @Override
    public Long getId() {
        return tb.getId();
    }

    @Override
    public UUID getResourceId() {
        return tb.getResourceId();
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
    public String getReason() {
        return tb.getReason();
    }

    // Setters

    public void setStartAt(LocalDateTime startAt) {
        tb.setStartAt(startAt);
    }

    public void setEndAt(LocalDateTime endAt) {
        tb.setEndAt(endAt);
    }

    public void setReason(String reason) {
        tb.setReason(reason);
    }
}
