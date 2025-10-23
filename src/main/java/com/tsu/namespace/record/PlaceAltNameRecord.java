package com.tsu.namespace.record;

import com.tsu.auth.security.AppSecurityContext;
import com.tsu.namespace.entities.PlaceAltNameTb;
import com.tsu.place.val.PlaceAltNameVal;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Record wrapper for PlaceAltNameTb entity.
 */
@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class PlaceAltNameRecord {

    @ToString.Include
    private final PlaceAltNameTb tb;
    private final AppSecurityContext context;

    public PlaceAltNameVal getValue() {
        return new PlaceAltNameVal(
                tb.getNamespaceId(),
                tb.getId(),
                tb.getPlaceId(),
                tb.getName(),
                tb.getLang(),
                tb.getType(),
                tb.getCreatedBy(),
                tb.getCreatedDate()
        );
    }

    public String getName() {
        return tb.getName();
    }

    public String getLang() {
        return tb.getLang();
    }

    public String getType() {
        return tb.getType();
    }
}
