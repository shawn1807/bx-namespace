package com.tsu.namespace.api.namespace;

import com.tsu.auth.api.AccessLevel;
import com.tsu.auth.api.BasePrincipal;
import com.tsu.auth.api.PermissionManager;
import com.tsu.auth.permissions.NamespaceAction;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.auth.security.NamespaceContext;
import com.tsu.common.api.ActionPack;
import com.tsu.common.api.EntryMetadataManager;
import com.tsu.common.api.MetadataManager;
import com.tsu.common.locale.EffectiveLocaleSettings;
import com.tsu.common.locale.EffectiveLocaleSettingsBuilder;
import com.tsu.common.utils.LazyCacheLoader;
import com.tsu.common.utils.ParamValidator;
import com.tsu.common.vo.Text;
import com.tsu.entry.api.EntryBucket;
import com.tsu.entry.api.EntryTextManager;
import com.tsu.entry.api.Node;
import com.tsu.entry.service.BucketService;
import com.tsu.enums.BaseParamName;
import com.tsu.namespace.api.*;
import com.tsu.namespace.api.formatter.FormatterImpl;
import com.tsu.namespace.api.manager.*;
import com.tsu.namespace.helper.*;
import com.tsu.namespace.record.NamespaceRecord;
import com.tsu.namespace.record.NamespaceRoleRecord;
import com.tsu.namespace.record.NamespaceUserRecord;
import com.tsu.namespace.security.NamespaceContextImpl;
import com.tsu.namespace.service.IDGeneratorService;
import com.tsu.namespace.val.*;
import com.tsu.place.api.PlaceManager;
import com.tsu.workspace.api.Formatter;
import com.tsu.workspace.api.TextManager;
import com.tsu.workspace.request.UserFilter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
public class NamespaceImpl implements Namespace {

    @ToString.Include
    private final NamespaceRecord value;
    private final LazyCacheLoader<EntryBucket> bucket;
    private final LazyCacheLoader<CalendarManager> calendarManager;
    private final LazyCacheLoader<PlaceManager> placeManager;
    private final LazyCacheLoader<ResourceManager> resourceManager;
    private final LazyCacheLoader<BookingManager> bookingManager;
    private final PermissionManager permissionManager;
    private final NamespaceDbHelper namespaceDbHelper;
    private final LazyCacheLoader<MetadataManager> metadataManager;
    private final LazyCacheLoader<EntityManager> entityManager;
    private final LazyCacheLoader<NumberManager> numberManager;
    private final LazyCacheLoader<TextManager> textManager;
    private final LazyCacheLoader<NamespaceUsers> namespaceUsers;
    private final AppSecurityContext context;
    private final NamespaceObjectFactory factory;
    private final NamespaceContext namespaceContext;


    public NamespaceImpl(AppSecurityContext context, NamespaceUser user, NamespaceRecord value, NamespaceDbHelper namespaceDbHelper,
                         EntityDbHelper entityDbHelper,
                         NumberDbHelper numberDbHelper, BucketService bucketService,
                         PlaceDbHelper placeDbHelper,
                         ResourceDbHelper resourceDbHelper,
                         BookingDbHelper bookingDbHelper,
                         SubscriptionDbHelper subscriptionDbHelper, AppDbHelper appDbHelper, IDGeneratorService idGeneratorService,
                         NamespaceObjectFactory factory) {
        this.value = value;
        this.namespaceDbHelper = namespaceDbHelper;
        this.context = context;
        this.factory = factory;
        this.bucket = LazyCacheLoader.of(() -> bucketService.findBucket(value.getBucket(), context.getBucketContext())
                .orElseThrow(() -> new IllegalStateException("bucket not exists"))
        );

        this.metadataManager = LazyCacheLoader.of(() -> new EntryMetadataManager(this));
        this.namespaceContext = new NamespaceContextImpl(context, user);
        this.numberManager = LazyCacheLoader.of(() -> new NamespaceNumberManager(namespaceContext, numberDbHelper, factory));
        this.entityManager = LazyCacheLoader.of(() -> new NamespaceEntityManager(namespaceContext,
                entityDbHelper, factory));
        this.permissionManager = new NamespacePermissionManager(namespaceContext, appDbHelper);
        this.placeManager = LazyCacheLoader.of(() -> new NamespacePlaceManager(this, context, placeDbHelper, factory));
        this.resourceManager = LazyCacheLoader.of(() -> new ResourceManagerImpl(this, context, resourceDbHelper, idGeneratorService));
        this.bookingManager = LazyCacheLoader.of(() -> new BookingManagerImpl(this, context, bookingDbHelper));
        this.calendarManager = LazyCacheLoader.of(() -> new NamespaceCalendarManager(this, context));
        this.textManager = LazyCacheLoader.of(() -> new EntryTextManager(bucket.get().getRoot(), permissionManager));
        this.namespaceUsers = LazyCacheLoader.of(() -> new CachedNamespaceUsers(namespaceDbHelper.findNamespaceJoinedUserInfoByNamespaceId(value.getId()).toList()));
    }


    @Override
    public TextManager getTextManager() {
        return textManager.get();
    }

    @Override
    public UUID getId() {
        return value.getId();
    }

    @Override
    public AppSecurityContext getContext() {
        return context;
    }

    @Override
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    @Override
    public NumberManager getNumberManager() {
        return numberManager.get();
    }

    @Override
    public MetadataManager getMetadataManager() {
        return metadataManager.get();
    }

    @Override
    public CalendarManager getCalendarManager() {
        return calendarManager.get();
    }

    @Override
    public PlaceManager getPlaceManager() {
        return placeManager.get();
    }

    @Override
    public ResourceManager getResourceManager() {
        return resourceManager.get();
    }

    @Override
    public BookingManager getBookingManager() {
        return bookingManager.get();
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager.get();
    }

    @Override
    public void setProps(Object props) {
        getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SET_NAMESPACE_PROPS, props));
        value.setProps(props);
        value.persist();
    }

    @Override
    public Node getNode() {
        return getBucket().getRoot();
    }

    @Override
    public EntryBucket getBucket() {
        return bucket.get();
    }

    @Override
    public <T> Optional<T> getProps(Class<T> type) {
        return Optional.ofNullable(value.getProps(type));
    }

    @Override
    public NamespaceVal getValue() {
        return value.getValue();
    }

    @Override
    public void setName(Text name) {
        ParamValidator.builder()
                .withNonNullOrEmpty(name, BaseParamName.NAME)
                .throwIfErrors();
        getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SET_NAMESPACE_NAME, name));
        value.setName(name.strip());
        value.persist();
    }

    @Override
    public Stream<NamespaceRoleVal> findRoles() {
        return namespaceDbHelper.findNamespaceRoles(getId(), namespaceContext)
                .map(NamespaceRoleRecord::getValue);
    }

    @Override
    public Stream<SubscriptionPlanVal> findSubscriptionPlans() {
        return Stream.empty();
    }

    @Override
    public Optional<NamespaceUser> findUser(BasePrincipal user) {
        return namespaceDbHelper.findNamespaceUserByNamespaceIdAndPrincipalId(getId(), user.id(), context)
                .map(r -> factory.build(value, r, context));
    }

    @Override
    public Stream<NamespaceUser> findUsers() {
        return namespaceDbHelper.findNamespaceUserByNamespaceId(getId(), namespaceContext)
                .map(r -> factory.build(value, r, context));
    }

    @Override
    public Stream<NamespaceUser> findUsers(NamespaceUserType type) {
        return namespaceDbHelper.findNamespaceUserByNamespaceIdAndType(getId(), type, namespaceContext)
                .map(r -> factory.build(value, r, context));
    }

    @Override
    public NamespaceUser addUser(UserBase user) {
        return addUser(user, NamespaceUserType.M);
    }

    @Override
    public NamespaceUser addUser(UserBase user, NamespaceUserType type) {
        NamespaceVal val = getValue();
        permissionManager.auditAndCheckPermission(new ActionPack(NamespaceAction.MANAGE_USER_SUBSCRIPTION, user));
        AccessLevel accessLevel = val.accessLevel();
        LocalDateTime approvedDate = null;
        LocalDate expirationDate = null;
        BasePrincipal approvedBy = null;
        boolean active = false;
        NamespaceUserRecord namespaceUserRecord = namespaceDbHelper.addNamespaceUser(getId(), user, user.getValue().displayName(), type,
                active, approvedDate, approvedBy, expirationDate, context);
        return factory.build(value, namespaceUserRecord, context);
    }

    @Override
    public Optional<NamespaceUser> findUserById(Integer userId) {
        return namespaceDbHelper.findNamespaceUserByNamespaceIdAndId(getId(), userId, namespaceContext)
                .map(user -> factory.build(value, user, context));

    }

    @Override
    public Optional<NamespaceRole> findRoleById(Integer roleId) {
        return Optional.empty();
    }

    @Override
    public NamespaceUsers getNamespaceUsers() {
        return namespaceUsers.get();
    }

    @Override
    public Page<NamespaceUserMvVal> queryUsers(UserFilter filter, Pageable pageable) {
        if (filter == null) {
            filter = UserFilter.builder().build();
        }
        NamespaceUsers users = getNamespaceUsers();
        return namespaceDbHelper.queryUsers(getId(), filter, pageable, namespaceContext)
                .map(tb -> new NamespaceUserMvVal(
                        tb.getId(),
                        tb.getRole(),
                        tb.getDisplayName(),
                        tb.getType(),
                        tb.isActive(),
                        tb.getActivationDate(),
                        tb.getExpirationDate(),
                        users.find(tb.getApprovedBy()).orElse(NspUsrVal.EMPTY).toDisplay(),
                        tb.getApprovedDate(),
                        users.find(tb.getCreatedBy()).orElse(NspUsrVal.EMPTY).toDisplay(),
                        tb.getCreatedDate(),
                        users.find(tb.getModifiedBy()).orElse(NspUsrVal.EMPTY).toDisplay(),
                        tb.getModifiedDate(),
                        tb.getFirstName(),
                        tb.getLastName(),
                        tb.getEmail(),
                        tb.getPhone(),
                        tb.getImageUrl()
                ));
    }

    // ========== LocaleSettings Implementation ==========

    @Override
    public Formatter getFormatter() {
        return new FormatterImpl(getSettings());
    }


    @Override
    public void setLocale(Locale locale) {
        log.info("Updating locale for namespace ID: {} to '{}'", getId(), locale.toLanguageTag());
        getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SET_NAMESPACE_PROPS, Map.of("locale", locale)));
        value.setLanguageTag(locale.toLanguageTag());
        value.persist();
        log.debug("Locale updated successfully for namespace ID: {}", getId());
    }

    @Override
    public void setZone(ZoneId zone) {
        log.info("Updating timezone for namespace ID: {} to '{}'", getId(), zone.getId());
        getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SET_NAMESPACE_PROPS, Map.of("timezone", zone)));
        value.setTimezoneId(zone.getId());
        value.persist();
        log.debug("Timezone updated successfully for namespace ID: {}", getId());
    }

    @Override
    public void setDateFormat(String dateFormat) {
        log.info("Updating date format for namespace ID: {} to '{}'", getId(), dateFormat);
        getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SET_NAMESPACE_PROPS, Map.of("dateFormat", dateFormat)));
        value.setDatePattern(dateFormat);
        value.persist();
        log.debug("Date format updated successfully for namespace ID: {}", getId());
    }


    @Override
    public EffectiveLocaleSettings getSettings() {
        return new EffectiveLocaleSettingsBuilder()
                .languageTag(value.getLanguageTag())
                .timezoneId(value.getTimezoneId())
                .datePattern(value.getDatePattern())
                .datetimePattern(value.getDatetimePattern())
                .build();
    }

    @Override
    public void setSettings(EffectiveLocaleSettings settings) {
        getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SET_NAMESPACE_PROPS, Map.of("settings", settings)));
        value.setLanguageTag(settings.getLanguageTag());
        value.setTimezoneId(settings.getTimezoneId());
        value.setDatePattern(settings.dateFormat());
        value.setDatetimePattern(settings.dateTimeFormat());
        value.persist();
    }

    @Override
    public void setDateTimeFormat(String dateTimeFormat) {
        getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SET_NAMESPACE_PROPS, Map.of("dateTimeFormat", dateTimeFormat)));
        value.setDatetimePattern(dateTimeFormat);
        value.persist();
    }

}
