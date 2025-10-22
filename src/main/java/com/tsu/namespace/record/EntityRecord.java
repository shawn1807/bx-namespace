package com.tsu.namespace.record;

import com.tsu.namespace.entities.EntityTb;
import com.tsu.workspace.val.EntityVal;
import com.tsu.common.jpa.JsonValueUtils;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.UUID;
import java.util.function.Consumer;


@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class EntityRecord {

    @ToString.Include
    private final EntityTb tb;
    private final Consumer<EntityTb> persist;

    public void persist() {
        persist.accept(tb);
    }

    public UUID getId() {
        return tb.getId().getId();
    }

    public EntityVal getValue() {
        return new EntityVal(tb.getName(), tb.getEmail(), tb.getPhone(), tb.getClazz(), tb.isActive(), tb.getCreatedBy(),
                tb.getCreatedDate(), tb.getModifiedBy(), tb.getModifiedDate());
    }

    public Integer getPrimaryPlaceId() {
        return tb.getPrimaryPlaceId();
    }

    public <T> T getProfile(Class<T> type) {
        return JsonValueUtils.getInstance().decode(tb.getProfile(), type);
    }

    public void setProfile(Object profile) {
        tb.setProfile(JsonValueUtils.getInstance().encodeAsJson(profile));
    }

    public void setName(String entityName) {
        tb.setName(entityName);
    }

    public void setEmail(String entityEmail) {
        tb.setEmail(entityEmail);
    }

    public Integer getEntityTypeId() {
        return tb.getTypeId();
    }

    public boolean isActive() {
        return tb.isActive();
    }

    public void setActive(boolean active) {
        tb.setActive(active);
    }

    public UUID getParentId() {
        return tb.getParentId();
    }

    public void setPhone(String phone) {
        tb.setPhone(phone);
    }
}
