package com.tsu.namespace.record;

import com.tsu.namespace.entities.NamespaceRoleTb;
import com.tsu.base.val.NamespaceRoleVal;
import com.tsu.common.jpa.JsonValueUtils;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class NamespaceRoleRecord {

    private final NamespaceRoleTb tb;
    private final Consumer<NamespaceRoleTb> persist;

    public void persist() {
        persist.accept(tb);
    }

    public NamespaceRoleVal getValue() {
        return new NamespaceRoleVal(getId(), tb.getName(), tb.getDescription());
    }

    public UUID getNamespaceId() {
        return tb.getNamespaceId();
    }

    public Integer getId() {
        return tb.getId();
    }


    public LocalDateTime getCreatedDate() {
        return tb.getCreatedDate();
    }


    public void setPermissions(Object permissions) {
        tb.setPermissions(JsonValueUtils.getInstance().encodeAsJson(permissions));
    }


    public <T> T getPermissions(Type type) {
        return JsonValueUtils.getInstance().decode(tb.getPermissions(), type);
    }


}
