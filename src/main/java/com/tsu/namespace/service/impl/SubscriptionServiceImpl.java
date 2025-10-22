package com.tsu.namespace.service.impl;

import com.tsu.auth.security.AppSecurityContext;
import com.tsu.auth.security.AppSecurityContextInitializer;
import com.tsu.common.exception.UserException;
import com.tsu.common.utils.ParamValidator;
import com.tsu.common.vo.Text;
import com.tsu.enums.BaseExceptionCode;
import com.tsu.enums.BaseParamName;
import com.tsu.namespace.api.Subscription;
import com.tsu.namespace.api.namespace.NamespaceObjectFactory;
import com.tsu.namespace.helper.NamespaceDbHelper;
import com.tsu.namespace.helper.SubscriptionDbHelper;
import com.tsu.namespace.record.NamespaceRecord;
import com.tsu.namespace.record.SubscriptionPlanRecord;
import com.tsu.namespace.record.SubscriptionRecord;
import com.tsu.namespace.service.SubscriptionService;
import com.tsu.workspace.request.ProvisionNamespace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {


    @Autowired
    private AppSecurityContextInitializer initializer;

    @Autowired
    private SubscriptionDbHelper dbHelper;

    @Autowired
    private NamespaceDbHelper namespaceDbHelper;

    @Autowired
    private NamespaceObjectFactory factory;


    @Override
    public Optional<Subscription> findSubscription(Text namespace) {
        log.debug("Finding subscription for namespace: {}", namespace);
        ParamValidator.builder()
                .withNonNullOrEmpty(namespace, BaseParamName.NAMESPACE)
                .throwIfErrors();
        AppSecurityContext context = initializer.initializeAndVerify();
        log.trace("Security context initialized for user: {}", context.getPrincipal().id());

        Optional<Subscription> subscription = namespaceDbHelper.findByName(namespace.strip(), context)
                .flatMap(nsp -> {
                    log.debug("Namespace found with ID: {}, searching for subscription", nsp.getId());
                    return dbHelper.findSubscriptionByNamespaceId(nsp.getId(), context)
                            .map(record -> {
                                Subscription sub = factory.build(record, context);
                                log.info("Subscription found for namespace: {} with plan: {}", namespace, record.getPlan().getName());
                                return sub;
                            });
                });

        if (subscription.isEmpty()) {
            log.warn("No subscription found for namespace: {}", namespace);
        }
        return subscription;
    }


    @Override
    public Subscription post(ProvisionNamespace provision) {
        log.info("Provisioning namespace: {} with plan: {}, recurring: {}",
                provision.getNamespace(), provision.getPlanName(), provision.isRecurring());

        ParamValidator.builder()
                .withNonNullOrEmpty(provision.getPlanName(), BaseParamName.SUBSCRIPTION_PLAN)
                .withNonNullOrEmpty(provision.getNamespace(), BaseParamName.NAMESPACE)
                .throwIfErrors();

        AppSecurityContext context = initializer.initializeAndVerify();
        log.trace("Security context initialized for provisioning by user: {}", context.getPrincipal().id());

        NamespaceRecord namespaceRecord = namespaceDbHelper.findByName(provision.getNamespace().strip(), context)
                .orElseThrow(() -> {
                    log.error("Namespace not found: {}", provision.getNamespace());
                    return new UserException(BaseExceptionCode.NAMESPACE_NOT_EXISTS);
                });

        log.debug("Found namespace: {} with ID: {}", namespaceRecord.getName(), namespaceRecord.getId());

        SubscriptionPlanRecord planRecord = dbHelper.findSubscriptionPlanByName(provision.getPlanName().strip())
                .orElseThrow(() -> {
                    log.error("Subscription plan not found: {}", provision.getPlanName());
                    return new UserException(BaseExceptionCode.SUBSCRIPTION_PLAN_NOT_EXISTS,
                            Map.of(BaseParamName.SUBSCRIPTION_PLAN, provision.getPlanName().strip()));
                });

        log.debug("Found subscription plan: {} with max users: {}",
                planRecord.getName(), planRecord.getMaxUser());

        LocalDate activationDate = Optional.ofNullable(provision.getActivationDate()).orElse(LocalDate.now());
        log.debug("Creating subscription with activation date: {}", activationDate);

        SubscriptionRecord subscriptionRecord = dbHelper.addSubscription(planRecord, namespaceRecord.getId(),
                provision.isRecurring(), activationDate, context);

        Subscription subscription = factory.build(subscriptionRecord, context);
        log.info("Successfully created subscription for namespace: {} with plan: {}",
                provision.getNamespace(), provision.getPlanName());

        return subscription;
    }


}
