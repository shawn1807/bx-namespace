package com.tsu.namespace.record;

import com.tsu.namespace.entities.NamespaceTb;
import com.tsu.base.enums.AccessLevel;
import com.tsu.base.val.NamespaceVal;
import com.tsu.common.api.BasePrincipal;
import com.tsu.common.jpa.JsonValueUtils;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class NamespaceRecord {

    private final NamespaceTb tb;
    private final Consumer<NamespaceTb> persist;


    public String getName() {
        return tb.getName();
    }

    public UUID getId() {
        return tb.getId();
    }

    public String getUri() {
        return tb.getUri();
    }

    public BasePrincipal getOwner() {
        return BasePrincipal.of(tb.getOwnerId());
    }


    public String getContactEmail() {
        return tb.getContactEmail();
    }

    public boolean isActive() {
        return tb.isActive();
    }

    public Optional<LocalDate> expirationDate() {
        return Optional.ofNullable(tb.getExpirationDate());
    }

    public String getDescription() {
        return tb.getDescription();
    }

    public String getBackgroundImageUrl() {
        return tb.getBackgroundImageUrl();
    }


    public AccessLevel getAccessLevel() {
        return tb.getAccessLevel();
    }

    public void setDescription(String description) {
        tb.setDescription(description);
    }

    public void setBackgroundImageUrl(String backgroundImageUrl) {
        tb.setBackgroundImageUrl(backgroundImageUrl);
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        tb.setAccessLevel(accessLevel);
    }

    public void setProps(Object obj) {
        tb.setProps(JsonValueUtils.getInstance().encodeAsJson(obj));
    }

    public void setName(String name) {
        tb.setName(name);
    }

    public BasePrincipal getCreateBy() {
        return BasePrincipal.of(tb.getCreatedBy());
    }

    public LocalDateTime getCreatedDate() {
        return tb.getCreatedDate();
    }

    public BasePrincipal getModifiedBy() {
        return BasePrincipal.of(tb.getModifiedBy());
    }

    public LocalDateTime getModifiedDate() {
        return tb.getModifiedDate();
    }

    public String getBucket() {
        return tb.getBucket();
    }


    public UUID getPrimaryWorkspaceId() {
        return tb.getPrimaryWorkspaceId();
    }

    public <T> T getProps(Class<T> type) {
        return JsonValueUtils.getInstance().decode(tb.getProps(), type);
    }

    public void persist() {
        persist.accept(tb);
    }

    public NamespaceVal getValue() {
        return new NamespaceVal(tb.getId(), tb.getName(), tb.getDescription(), tb.getUri(), tb.getContactEmail(), tb.getBackgroundImageUrl(),
                getOwner(), tb.getAccessLevel(),
                tb.isActive(), tb.getExpirationDate(), getCreateBy(),
                tb.getCreatedDate(), getModifiedBy(), getModifiedDate());
    }


    public void setPrimaryWorkspaceId(UUID id) {
        tb.setPrimaryWorkspaceId(id);
    }
}
