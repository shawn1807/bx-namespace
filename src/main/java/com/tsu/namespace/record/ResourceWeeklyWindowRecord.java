package com.tsu.namespace.record;

import com.tsu.auth.security.AppSecurityContext;
import com.tsu.namespace.api.ResourceWeeklyWindow;
import com.tsu.namespace.entities.ResourceWeeklyWindowTb;
import com.tsu.namespace.val.ResourceWeeklyWindowVal;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Record wrapper for ResourceWeeklyWindowTb entity.
 */
@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class ResourceWeeklyWindowRecord implements ResourceWeeklyWindow {

    @ToString.Include
    private final ResourceWeeklyWindowTb tb;
    private final AppSecurityContext context;

    public ResourceWeeklyWindowVal getValue() {
        return new ResourceWeeklyWindowVal(
                tb.getNamespaceId(),
                tb.getId(),
                tb.getResourceId(),
                tb.getDayOfWeek(),
                tb.getStartLocal(),
                tb.getEndLocal()
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
    public Integer getDayOfWeek() {
        return tb.getDayOfWeek();
    }

    @Override
    public LocalTime getStartLocal() {
        return tb.getStartLocal();
    }

    @Override
    public LocalTime getEndLocal() {
        return tb.getEndLocal();
    }

    // Setters

    public void setDayOfWeek(Integer dayOfWeek) {
        tb.setDayOfWeek(dayOfWeek);
    }

    public void setStartLocal(LocalTime startLocal) {
        tb.setStartLocal(startLocal);
    }

    public void setEndLocal(LocalTime endLocal) {
        tb.setEndLocal(endLocal);
    }
}
