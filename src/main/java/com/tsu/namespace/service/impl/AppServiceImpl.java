package com.tsu.namespace.service.impl;

import com.tsu.auth.api.BasePrincipal;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.auth.security.AppSecurityContextInitializer;
import com.tsu.auth.security.NamespaceContext;
import com.tsu.common.api.PrincipalType;
import com.tsu.common.exception.UserException;
import com.tsu.common.upgrades.AppModule;
import com.tsu.common.utils.ParamValidator;
import com.tsu.common.vo.Email;
import com.tsu.common.vo.Text;
import com.tsu.entry.api.EntryBucket;
import com.tsu.entry.service.BucketService;
import com.tsu.enums.BaseExceptionCode;
import com.tsu.enums.BaseParamName;
import com.tsu.namespace.api.NamespaceUser;
import com.tsu.namespace.api.NamespaceUserType;
import com.tsu.namespace.api.UserBase;
import com.tsu.namespace.api.namespace.NamespaceObjectFactory;
import com.tsu.namespace.helper.AppModuleDbHelper;
import com.tsu.namespace.helper.NamespaceDbHelper;
import com.tsu.namespace.helper.UserDbHelper;
import com.tsu.namespace.record.AppModuleRecord;
import com.tsu.namespace.record.NamespaceRecord;
import com.tsu.namespace.record.NamespaceUserRecord;
import com.tsu.namespace.security.NamespaceContextImpl;
import com.tsu.namespace.service.AppService;
import com.tsu.namespace.service.IDGeneratorService;
import com.tsu.namespace.service.SubscriptionService;
import com.tsu.namespace.service.UserService;
import com.tsu.namespace.upgrades.impl.AppModuleImpl;
import com.tsu.namespace.val.NamespaceVal;
import com.tsu.workspace.request.AddNamespace;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
public class AppServiceImpl implements AppService {


    @Autowired
    private AppSecurityContextInitializer initializer;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private NamespaceDbHelper namespaceDbHelper;


    @Autowired
    private NamespaceObjectFactory factory;

    @Autowired
    private BucketService bucketService;

    @Autowired
    private SubscriptionService subscriptionService;


    @Autowired
    private AppModuleDbHelper moduleDbHelper;


    @Autowired
    private UserDbHelper userDbHelper;

    @Autowired
    private IDGeneratorService idGeneratorService;


    @Override
    public Stream<AppModule> getModules() {
        AppSecurityContext context = initializer.initializeAndVerify();
        return moduleDbHelper.findAllAppModules()
                .map(module -> new AppModuleImpl(module, moduleDbHelper, this, bucketService, userService, applicationContext, transactionManager, context));
    }

    @Override
    public Optional<AppModule> findModule(String name) {
        AppSecurityContext context = initializer.initializeAndVerify();
        return moduleDbHelper.findAppModule(name)
                .map(module -> new AppModuleImpl(module, moduleDbHelper, this, bucketService, userService, applicationContext, transactionManager, context));
    }

    @Transactional
    @Override
    public AppModule installModule(String name, String version, Integer build, String buildPackage) {
        ParamValidator.builder()
                .withNonNullOrEmpty(name, BaseParamName.NAME)
                .withNonNullOrEmpty(version, BaseParamName.VERSION)
                .withNonNullOrEmpty(buildPackage, BaseParamName.PACKAGE)
                .throwIfErrors();
        AppSecurityContext context = initializer.initializeAndVerify();
        moduleDbHelper.findAppModule(name)
                .ifPresent(m -> {
                    throw new UserException(BaseExceptionCode.MODULE_EXISTS, Map.of(BaseParamName.NAME, name));
                });
        AppModuleRecord module = moduleDbHelper.createModule(name, version, build, buildPackage);
        return new AppModuleImpl(module, moduleDbHelper, this, bucketService, userService, applicationContext, transactionManager, context);
    }

    @Override
    public NamespaceContext post(AddNamespace post, Object props) {
        log.info("Creating new namespace '{}' with URI '{}' in bucket '{}'", post.getName().strip(), post.getUri(), post.getBucket());
        log.debug("Namespace creation request details: {}", post);
        ParamValidator.builder()
                .withNonNullOrEmpty(post.getName(), BaseParamName.NAME)
                .withNonNullOrEmpty(post.getUri(), BaseParamName.URI)
                .withNonNullOrEmpty(post.getBucket(), BaseParamName.BUCKET)
                .withNonNull(post.getContactEmail(), BaseParamName.EMAIL)
                .withVerifyEmail(post.getContactEmail())
                .throwIfErrors();
        log.debug("Namespace creation parameters validated successfully");
        AppSecurityContext context = initializer.initializeAndVerify();
        log.debug("Locating entry bucket: {}", post.getBucket());
        EntryBucket entryBucket = bucketService.findBucket(post.getBucket(), context.getBucketContext())
                .orElseThrow(() -> {
                    log.error("Entry bucket not found: {}", post.getBucket());
                    return new UserException(BaseExceptionCode.MISSING_BUCKET, Map.of(BaseParamName.BUCKET, post.getBucket()));
                });
        log.debug("Entry bucket located successfully: {}", entryBucket.getName());
        BasePrincipal contextUser = context.getPrincipal();
        String contactEmail = Optional.ofNullable(post.getContactEmail())
                .map(Email::value)
                .map(Text::strip)
                .orElseGet(() -> context.getUser()
                        .map(UserBase::getEmail)
                        .orElseThrow()
                );
        BasePrincipal basePrincipal = userDbHelper.createBasePrincipal(post.getName().strip(), PrincipalType.NAMESPACE);
        NamespaceRecord record = namespaceDbHelper.createNamespace(basePrincipal, post.getName().strip(), post.getUri(), post.getWebsite(), contactEmail,
                entryBucket.getName(), post.getDescription().strip(), post.getLogoImageUrl(),
                post.getBackgroundImageUrl(),
                post.getAccessLevel(), props, context);
        NamespaceUserRecord namespaceUserRecord = namespaceDbHelper.addNamespaceUser(basePrincipal.id(), contextUser,
                context.getName(), NamespaceUserType.M, true, LocalDateTime.now(), contextUser,
                null, context);
        log.debug("Namespace record created with ID: {}", record.getId());
        NamespaceUser namespaceUser = factory.build(record, namespaceUserRecord, context);
        NamespaceContext namespaceContext = new NamespaceContextImpl(context, namespaceUser);
        log.info("Namespace '{}' created successfully with ID: {}", record.getName(), basePrincipal);
        return namespaceContext;
    }


    @Override
    public Optional<NamespaceContext> findNamespaceContextByUri(Text uri) {
        ParamValidator.builder()
                .withNonNullOrEmpty(uri, BaseParamName.URI)
                .throwIfErrors();
        AppSecurityContext context = initializer.initializeAndVerify();
        return namespaceDbHelper.findByUri(uri.strip(), context)
                .flatMap(namespaceRecord -> namespaceDbHelper.findNamespaceUserByNamespaceIdAndPrincipalId(namespaceRecord.getId(), context.getPrincipal().id(), context)
                        .map(userRecord -> factory.build(namespaceRecord, userRecord, context)))
                .filter(NamespaceUser::isValid)
                .map(user -> new NamespaceContextImpl(context, user));

    }

    @Override
    public Optional<UUID> findNamespaceIdByUri(Text uri) {
        ParamValidator.builder()
                .withNonNullOrEmpty(uri, BaseParamName.URI)
                .throwIfErrors();
        return namespaceDbHelper.checkUriAvailabilityForNamespace(uri.strip());
    }

    @Override
    public Optional<UUID> findNamespaceIdByName(Text name) {
        ParamValidator.builder()
                .withNonNullOrEmpty(name, BaseParamName.NAME)
                .throwIfErrors();
        return namespaceDbHelper.checkNameAvailabilityForNamespace(name.strip());
    }

    @Override
    public Optional<NamespaceContext> findNamespaceContextById(UUID namespaceId) {
        AppSecurityContext context = initializer.initializeAndVerify();
        UUID userId = context.getPrincipal().id();
        return namespaceDbHelper.findNamespaceUserByNamespaceIdAndPrincipalId(namespaceId, userId, context)
                .flatMap(user -> namespaceDbHelper.findById(user.getNamespaceId(), context)
                        .map(namespaceRecord -> factory.build(namespaceRecord, user, context)))
                .filter(NamespaceUser::isValid)
                .map(user -> new NamespaceContextImpl(context, user));

    }


    @Override
    public Stream<NamespaceVal> findJoinedNamespaces(BasePrincipal principal) {
        AppSecurityContext context = initializer.initializeAndVerify();
        return namespaceDbHelper.findByPrincipalId(principal.id(), context)
                .map(NamespaceRecord::getValue);
    }


}
