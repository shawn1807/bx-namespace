package com.tsu.namespace.record;

import com.tsu.auth.api.BasePrincipal;
import com.tsu.common.api.Entry;
import com.tsu.common.jpa.JsonValueUtils;
import com.tsu.namespace.api.NamespaceUserType;
import com.tsu.namespace.entities.NamespaceRoleTb;
import com.tsu.namespace.entities.NamespaceUserTb;
import com.tsu.namespace.val.NamespaceUserVal;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class NamespaceUserRecord {


    private final NamespaceUserTb tb;
    private final Consumer<NamespaceUserTb> persist;
    private final Consumer<NamespaceRoleTb> persistRole;

    public void persist() {
        persist.accept(tb);
    }

    public NamespaceUserVal getValue() {
        return new NamespaceUserVal(getId(), getUser(), isActive(), getActivationDate(), getExpirationDate(),
                getCreatedBy(), getCreatedDate(), getModifiedBy(), getModifiedDate());
    }

    public Optional<NamespaceRoleRecord> getRole() {
        return Optional.ofNullable(tb.getRole())
                .map(r -> new NamespaceRoleRecord(r, persistRole));
    }

    public UUID getNamespaceId() {
        return tb.getId().getNamespaceId();
    }

    public Integer getId() {
        return tb.getId().getId();
    }

    public BasePrincipal getUser() {
        return BasePrincipal.of(tb.getPrincipalId());
    }


    public boolean isActive() {
        return tb.isActive();
    }

    public LocalDate getActivationDate() {
        return tb.getActivationDate();
    }

    public LocalDate getExpirationDate() {
        return tb.getExpirationDate();
    }

    public LocalDateTime getCreatedDate() {
        return tb.getCreatedDate();
    }

    public BasePrincipal getCreatedBy() {
        return BasePrincipal.of(tb.getCreatedBy());
    }

    public LocalDateTime getModifiedDate() {
        return tb.getModifiedDate();
    }

    public BasePrincipal getModifiedBy() {
        return BasePrincipal.of(tb.getModifiedBy());
    }

    public void setActive(boolean active) {
        tb.setActive(active);
    }


    public void setApprovedDate(LocalDateTime approvedDate) {
        tb.setApprovedDate(approvedDate);
    }

    public void setApprovedBy(BasePrincipal approvedBy) {
        tb.setApprovedBy(approvedBy.id());
    }

    public void setExpirationDate(LocalDate expiryOn) {
        tb.setExpirationDate(expiryOn);
    }

    public NamespaceUserType getType() {
        return tb.getType();
    }

    public void setPermissions(Object permissions) {
        tb.setPermissions(JsonValueUtils.getInstance().encodeAsJson(permissions));
    }

    public <T> T getPermissions(Type type) {
        return JsonValueUtils.getInstance().decode(tb.getPermissions(), type);
    }
}
