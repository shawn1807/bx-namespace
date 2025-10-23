package com.tsu.namespace.record;

import com.tsu.auth.security.AppSecurityContext;
import com.tsu.common.jpa.JsonValueUtils;
import com.tsu.namespace.entities.PlaceTb;
import com.tsu.place.val.PlaceVal;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Record wrapper for PlaceTb entity.
 */
@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class PlaceRecordEnhanced {

    @ToString.Include
    private final PlaceTb tb;
    private final AppSecurityContext context;

    public PlaceVal getValue() {
        return new PlaceVal(
                tb.getId().getNamespaceId(),
                tb.getId().getId(),
                tb.getParentId(),
                tb.getType(),
                tb.getIsoCountry(),
                tb.getCountry(),
                tb.getCounty(),
                tb.getCity(),
                tb.getBuilding(),
                tb.getAddress(),
                tb.getPostCode(),
                tb.getName(),
                tb.getLat(),
                tb.getLng(),
                tb.getProps() != null ? tb.getProps().toString() : null,
                tb.getNotes(),
                tb.isActive(),
                tb.getCreatedBy(),
                tb.getCreatedDate(),
                tb.getModifiedBy(),
                tb.getModifiedDate()
        );
    }

    public PlaceTb getEntity() {
        return tb;
    }

    // Getters delegating to entity
    public java.util.UUID getNamespaceId() {
        return tb.getId().getNamespaceId();
    }

    public java.util.UUID getId() {
        return tb.getId().getId();
    }

    public String getName() {
        return tb.getName();
    }

    public String getType() {
        return tb.getType();
    }

    public Double getLat() {
        return tb.getLat();
    }

    public Double getLng() {
        return tb.getLng();
    }

    public <T> T getProps(Class<T> type) {
        if (tb.getProps() == null) return null;
        return JsonValueUtils.getInstance().decode(tb.getProps().toString(), type);
    }

    public boolean isActive() {
        return tb.isActive();
    }
}
