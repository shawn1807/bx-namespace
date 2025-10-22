package com.tsu.namespace.helper;

import com.tsu.enums.BaseExceptionCode;
import com.tsu.enums.BaseParamName;
import com.tsu.namespace.entities.SubscriptionPlanTb;
import com.tsu.namespace.entities.SubscriptionTb;
import com.tsu.namespace.entities.id.SubscriptionId;
import com.tsu.namespace.entities.id.SubscriptionPlanId;
import com.tsu.namespace.record.SubscriptionPlanRecord;
import com.tsu.namespace.record.SubscriptionRecord;
import com.tsu.namespace.repo.NamespaceUserRepository;
import com.tsu.namespace.repo.SubscriptionHistoryRepository;
import com.tsu.namespace.repo.SubscriptionPlanRepository;
import com.tsu.namespace.repo.SubscriptionRepository;
import com.tsu.namespace.val.SubscriptionHistoryVal;
import com.tsu.namespace.val.SubscriptionPlanVal;
import com.tsu.auth.api.BasePrincipal;
import com.tsu.common.exception.UserException;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.workspace.config.DurationUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Component
public class SubscriptionDbHelper {

    private final SubscriptionRepository subscriptionRepository;

    private final SubscriptionHistoryRepository historyRepository;

    private final SubscriptionPlanRepository planRepository;

    private final NamespaceUserRepository namespaceUserRepository;


    public Optional<SubscriptionPlanRecord> findSubscriptionPlanByName(String name) {
        log.debug("Looking up subscription plan by name: {}", name);
        Optional<SubscriptionPlanRecord> plan = planRepository.findByName(name)
                .map(tb -> {
                    log.trace("Subscription plan found: {} with ID: {}", name, tb.getId());
                    return new SubscriptionPlanRecord(tb);
                });
        if (plan.isEmpty()) {
            log.warn("No subscription plan found with name: {}", name);
        }
        return plan;
    }

    public Optional<SubscriptionPlanRecord> findSubscriptionPlanById(UUID namespaceId, Integer id) {
        log.debug("Looking up subscription plan by ID: {}", id);
        SubscriptionPlanId planId = new SubscriptionPlanId(namespaceId, id);
        Optional<SubscriptionPlanRecord> plan = planRepository.findById(planId)
                .map(tb -> {
                    log.trace("Subscription plan found with ID: {} and name: {}", id, tb.getName());
                    return new SubscriptionPlanRecord(tb);
                });
        if (plan.isEmpty()) {
            log.warn("No subscription plan found with ID: {}", id);
        }
        return plan;
    }

    public SubscriptionPlanRecord addSubscriptionPlan(String name, String description, DurationUnit unit, Integer duration, int maxUsers) {
        log.info("Creating new subscription plan: {} with {} users for {} {}", name, maxUsers, duration, unit);
        log.debug("Plan details - description: {}, duration: {} {}", description, duration, unit);

        SubscriptionPlanTb tb = new SubscriptionPlanTb();
        tb.setName(name);
        tb.setDescription(description);
        tb.setDurationUnit(unit);
        tb.setDuration(duration);
        tb.setMaxUser(maxUsers);

        log.debug("Saving subscription plan to database: {}", name);
        planRepository.save(tb);
        log.info("Subscription plan created successfully - ID: {}, name: {}", tb.getId(), name);

        return new SubscriptionPlanRecord(tb);
    }

    public SubscriptionRecord addSubscription(SubscriptionPlanRecord plan, UUID namespaceId, boolean recurring, LocalDate activationDate,
                                              AppSecurityContext context) {
        SubscriptionPlanVal value = plan.getValue();
        log.info("Creating subscription for namespace: {} with plan: {} (recurring: {}, activation: {})",
                namespaceId, value.name(), recurring, activationDate);
        log.debug("Plan details - maxUsers: {}, period: {}", value.maxUser(), value.period());

        SubscriptionTb tb = new SubscriptionTb();
        SubscriptionId id = new SubscriptionId();
        id.setNamespaceId(namespaceId);
        tb.setId(id);
        tb.setPlanId(value.id());
        tb.setRecurring(recurring);
        tb.setActivationDate(activationDate);
        tb.setCreatedBy(context.getPrincipal().id());
        tb.setCreatedDate(LocalDateTime.now());
        tb.setModifiedBy(context.getPrincipal().id());
        tb.setModifiedDate(LocalDateTime.now());

        log.debug("Saving subscription to database for namespace: {}", namespaceId);
        subscriptionRepository.save(tb);
        log.info("Subscription created successfully - ID: {}, namespace: {}, plan: {}",
                tb.getId(), namespaceId, value.name());

        return new SubscriptionRecord(tb, plan, record -> {
            log.trace("Persisting subscription changes for ID: {} by user: {}", record.getId(), context.getPrincipal().id());
            record.setModifiedDate(LocalDateTime.now());
            record.setModifiedBy(context.getPrincipal().id());
            subscriptionRepository.save(record);
            log.debug("Subscription persisted successfully: {}", record.getId());
        });
    }

    public Optional<SubscriptionRecord> findSubscriptionByNamespaceId(UUID id, AppSecurityContext context) {
        log.debug("Looking up subscription for namespace ID: {}", id);
        return subscriptionRepository.findByIdNamespaceId(id)
                .map(tb -> {
                    log.debug("Subscription found for namespace: {} with plan ID: {}", id, tb.getPlanId());
                    SubscriptionPlanRecord plan = findSubscriptionPlanById(id, tb.getPlanId())
                            .orElseThrow(() -> {
                                log.error("Subscription plan not found for ID: {} in namespace: {}", tb.getPlanId(), id);
                                return new UserException(BaseExceptionCode.SUBSCRIPTION_PLAN_NOT_EXISTS,
                                        Map.of(BaseParamName.SUBSCRIPTION_PLAN, tb.getPlanId()));
                            });
                    log.trace("Subscription record created for namespace: {} with plan: {}", id, plan.getValue().name());
                    return new SubscriptionRecord(tb, plan, record -> {
                        log.trace("Persisting subscription changes for namespace: {} by user: {}", id, context.getPrincipal().id());
                        record.setModifiedDate(LocalDateTime.now());
                        record.setModifiedBy(context.getPrincipal().id());
                        subscriptionRepository.save(record);
                        log.debug("Subscription persisted for namespace: {}", id);
                    });
                });
    }

    public Optional<SubscriptionRecord> findSubscriptionById(UUID namespaceId, Integer id, AppSecurityContext context) {
        SubscriptionId subscriptionId = new SubscriptionId(namespaceId, id);
        return subscriptionRepository.findById(subscriptionId)
                .map(tb -> {
                    SubscriptionPlanRecord plan = findSubscriptionPlanById(namespaceId, tb.getPlanId())
                            .orElseThrow(() -> new UserException(BaseExceptionCode.SUBSCRIPTION_PLAN_NOT_EXISTS, Map.of(BaseParamName.SUBSCRIPTION_PLAN, tb.getPlanId())));
                    return new SubscriptionRecord(tb, plan, record -> {
                        record.setModifiedDate(LocalDateTime.now());
                        record.setModifiedBy(context.getPrincipal().id());
                        subscriptionRepository.save(record);
                    });
                });
    }


    public Stream<SubscriptionHistoryVal> findSubscriptionHistoryByNamespaceId(UUID namespaceId) {
        return historyRepository.findByIdNamespaceIdOrderByCreatedDateAsc(namespaceId)
                .map(h -> new SubscriptionHistoryVal(h.getId().getId(), h.getPlan(), BasePrincipal.of(h.getProviderId()),
                        h.getActivationDate(), h.getExpirationDate(), BasePrincipal.of(h.getCreatedBy()), h.getCreatedDate()));
    }
}
