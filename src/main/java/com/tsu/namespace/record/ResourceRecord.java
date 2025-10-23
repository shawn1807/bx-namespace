package com.tsu.namespace.record;

import com.tsu.auth.security.AppSecurityContext;
import com.tsu.namespace.api.Resource;
import com.tsu.namespace.api.ResourceType;
import com.tsu.namespace.entities.ResourceTb;
import com.tsu.namespace.val.ResourceVal;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Record wrapper for ResourceTb entity implementing Resource interface.
 */
@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class ResourceRecord implements Resource {

    @ToString.Include
    private final ResourceTb tb;
    private final AppSecurityContext context;

    public ResourceVal getValue() {
        return new ResourceVal(
                tb.getId().getNamespaceId(),
                tb.getId().getId(),
                tb.getType(),
                tb.getName(),
                tb.getCapacity(),
                tb.getLocation(),
                tb.getTimezone(),
                tb.getMeta(),
                tb.isActive(),
                tb.getCreatedAt(),
                tb.getCreatedBy(),
                tb.getUpdatedAt(),
                tb.getUpdatedBy()
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
    public ResourceType getType() {
        return tb.getType();
    }

    @Override
    public String getName() {
        return tb.getName();
    }

    @Override
    public Integer getCapacity() {
        return tb.getCapacity();
    }

    @Override
    public String getLocation() {
        return tb.getLocation();
    }

    @Override
    public String getTimezone() {
        return tb.getTimezone();
    }

    @Override
    public String getMeta() {
        return tb.getMeta();
    }

    @Override
    public boolean isActive() {
        return tb.isActive();
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

    // Setters for mutable fields

    public void setName(String name) {
        tb.setName(name);
    }

    public void setCapacity(Integer capacity) {
        tb.setCapacity(capacity);
    }

    public void setLocation(String location) {
        tb.setLocation(location);
    }

    public void setTimezone(String timezone) {
        tb.setTimezone(timezone);
    }

    public void setMeta(String meta) {
        tb.setMeta(meta);
    }

    public void setActive(boolean active) {
        tb.setActive(active);
    }
}
