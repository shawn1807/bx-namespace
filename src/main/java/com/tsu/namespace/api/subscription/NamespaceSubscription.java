package com.tsu.namespace.api.subscription;

import com.tsu.namespace.api.Subscription;
import com.tsu.namespace.helper.NamespaceDbHelper;
import com.tsu.namespace.helper.SubscriptionDbHelper;
import com.tsu.namespace.record.NamespaceRecord;
import com.tsu.namespace.record.SubscriptionRecord;
import com.tsu.namespace.val.NamespaceVal;
import com.tsu.namespace.val.SubscriptionHistoryVal;
import com.tsu.namespace.val.SubscriptionPlanVal;
import com.tsu.namespace.val.SubscriptionVal;
import com.tsu.common.utils.LazyCacheLoader;
import com.tsu.auth.security.AppSecurityContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class NamespaceSubscription implements Subscription {

    private final SubscriptionRecord record;
    private final AppSecurityContext context;
    private final NamespaceDbHelper namespaceDbHelper;
    private final LazyCacheLoader<List<SubscriptionHistoryVal>> histories;

    public NamespaceSubscription(SubscriptionRecord record, AppSecurityContext context,
                                 SubscriptionDbHelper subscriptionDbHelper, NamespaceDbHelper namespaceDbHelper
    ) {
        this.record = record;
        this.context = context;
        this.namespaceDbHelper = namespaceDbHelper;
        this.histories = LazyCacheLoader.of(() -> subscriptionDbHelper.findSubscriptionHistoryByNamespaceId(record.getNamespaceId())
                .toList());
    }

    @Override
    public SubscriptionPlanVal getPlan() {
        return record.getPlan().getValue();
    }

    @Override
    public NamespaceVal getNamespace() {
        return namespaceDbHelper.findById(record.getNamespaceId(), context)
                .map(NamespaceRecord::getValue)
                .orElseThrow(() -> new IllegalStateException("namespace not found"));
    }

    @Override
    public boolean isValid() {
        LocalDate today = LocalDate.now();
        Boolean activated = Optional.ofNullable(record.getActivationDate())
                .map(today::isAfter)
                .orElse(false);
        Boolean expired = Optional.ofNullable(record.getExpirationDate())
                .map(today::isAfter)
                .orElse(true);
        return activated && !expired;
    }

    @Override
    public void cancelImmediately() {
        record.setExpirationDate(LocalDate.now());
        record.persist();
    }


    @Override
    public Stream<SubscriptionHistoryVal> getHistories() {
        return histories.get().stream();
    }

    @Override
    public SubscriptionVal getValue() {
        return record.getValue();
    }
}
