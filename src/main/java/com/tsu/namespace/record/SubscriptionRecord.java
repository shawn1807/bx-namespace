package com.tsu.namespace.record;

import com.tsu.namespace.entities.SubscriptionTb;
import com.tsu.namespace.val.SubscriptionVal;
import com.tsu.auth.api.BasePrincipal;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class SubscriptionRecord {

    private final SubscriptionTb tb;
    private final SubscriptionPlanRecord subscriptionPlanRecord;
    private final Consumer<SubscriptionTb> persist;

    public void persist() {
        persist.accept(tb);
    }

    public SubscriptionVal getValue() {
        return new SubscriptionVal(getId(), subscriptionPlanRecord.getValue(), tb.isRecurring(), getActivationDate(),
                getExpirationDate(), getCreatedBy(),
                getCreatedDate(), getModifiedBy(), getModifiedDate());
    }

    public SubscriptionPlanRecord getPlan() {
        return subscriptionPlanRecord;
    }

    public UUID getNamespaceId() {
        return tb.getId().getNamespaceId();
    }

    public Integer getId() {
        return tb.getId().getId();
    }

    public String getDescription() {
        return subscriptionPlanRecord.getDescription();
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

    public void setExpirationDate(LocalDate expiry) {
        tb.setExpirationDate(expiry);
    }
}
