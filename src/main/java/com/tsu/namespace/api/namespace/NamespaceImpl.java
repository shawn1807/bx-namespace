package com.tsu.namespace.api.namespace;

import com.tsu.namespace.api.*;
import com.tsu.namespace.api.manager.*;
import com.tsu.namespace.entities.NamespaceUserViewTb;
import com.tsu.base.enums.AccessLevel;
import com.tsu.base.enums.BaseCustomType;
import com.tsu.base.enums.BaseParamName;
import com.tsu.base.enums.NamespaceAction;
import com.tsu.base.request.UserFilter;
import com.tsu.namespace.helper.*;
import com.tsu.namespace.record.NamespaceRecord;
import com.tsu.namespace.record.NamespaceRoleRecord;
import com.tsu.namespace.record.NamespaceUserRecord;
import com.tsu.namespace.security.NamespaceContextImpl;
import com.tsu.base.service.IDGeneratorService;
import com.tsu.base.val.NamespaceRoleVal;
import com.tsu.base.val.NamespaceUserMvVal;
import com.tsu.base.val.NamespaceVal;
import com.tsu.base.val.SubscriptionPlanVal;
import com.tsu.common.api.ActionPack;
import com.tsu.common.api.BasePrincipal;
import com.tsu.common.utils.LazyCacheLoader;
import com.tsu.common.utils.ParamValidator;
import com.tsu.common.vo.Text;
import com.tsu.entry.api.EntryBucket;
import com.tsu.entry.api.Node;
import com.tsu.entry.service.BucketService;
import com.tsu.security.AppSecurityContext;
import com.tsu.security.NamespaceContext;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
public class NamespaceImpl implements Namespace {

    @ToString.Include
    private final NamespaceRecord value;
    private final LazyCacheLoader<EntryBucket> bucket;
    private final LazyCacheLoader<Workspace> primaryWorkspace;
    private final LazyCacheLoader<CalendarManager> calendarManager;
    private final LazyCacheLoader<PlaceManager> placeManager;
    private final PermissionManager permissionManager;
    private final NamespaceDbHelper namespaceDbHelper;
    private final LazyCacheLoader<MetadataManager> metadataManager;
    private final LazyCacheLoader<EntityManager> entityManager;
    private final LazyCacheLoader<WorkspaceManager> workspaceManager;
    private final LazyCacheLoader<NumberManager> numberManager;
    private final LazyCacheLoader<TextManager> textManager;
    private final LazyCacheLoader<NamespaceUsers> namespaceUsers;
    private final AppSecurityContext context;
    private final DomainObjectBuilder builder;
    private final NamespaceContext namespaceContext;


    public NamespaceImpl(AppSecurityContext context, NamespaceUser user, NamespaceRecord value, NamespaceDbHelper namespaceDbHelper,
                         WorkspaceDbHelper workspaceDatabaseHelper, EntityDbHelper entityDbHelper, WorkDbHelper taskDbHelper,
                         NumberDbHelper numberDbHelper, BucketService bucketService,
                         PlaceDbHelper placeDbHelper, DocumentDbHelper documentDbHelper,
                         SubscriptionDbHelper subscriptionDbHelper, AppDbHelper appDbHelper, IDGeneratorService idGeneratorService,
                         DomainObjectBuilder builder) {
        this.value = value;
        this.namespaceDbHelper = namespaceDbHelper;
        this.context = context;
        this.builder = builder;
        this.bucket = LazyCacheLoader.of(() -> bucketService.findBucket(value.getBucket(), context.getBucketContext())
                .orElseThrow(() -> new IllegalStateException("bucket not exists"))
        );
        this.primaryWorkspace = LazyCacheLoader.of(() -> getWorkspaceManager().findWorkspaceById(value.getPrimaryWorkspaceId())
                .orElseThrow(() -> new IllegalStateException("bucket not exists"))
        );

        this.metadataManager = LazyCacheLoader.of(() -> new EntryMetadataManager(this));
        this.namespaceContext = new NamespaceContextImpl(context, user);
        this.numberManager = LazyCacheLoader.of(() -> new NamespaceNumberManager(namespaceContext, numberDbHelper, builder));
        this.entityManager = LazyCacheLoader.of(() -> new NamespaceEntityManager(namespaceContext,
                entityDbHelper, builder));
        this.permissionManager = new NamespacePermissionManager(namespaceContext, appDbHelper, builder);
        this.placeManager = LazyCacheLoader.of(() -> new NamespacePlaceManager(this, context, placeDbHelper, builder));
        this.calendarManager = LazyCacheLoader.of(() -> new NamespaceCalendarManager(this, context));
        this.textManager = LazyCacheLoader.of(() -> new EntryTextManager(bucket.get().getRoot(), permissionManager));
        this.workspaceManager = LazyCacheLoader.of(() -> new NamespaceWorkspaceManager(namespaceContext, workspaceDatabaseHelper, taskDbHelper,
                documentDbHelper, idGeneratorService, builder));
        this.namespaceUsers = LazyCacheLoader.of(() -> new CachedNamespaceUsers(namespaceDbHelper.findNamespaceJoinedUserInfoByNamespaceId(value.getId())));
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
    public WorkspaceManager getWorkspaceManager() {
        return workspaceManager.get();
    }


    @Override
    public Workspace getPrimaryWorkspace() {
        return primaryWorkspace.get();
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
                .map(r -> builder.build(value, r, context));
    }

    @Override
    public Stream<NamespaceUser> findUsers() {
        return namespaceDbHelper.findNamespaceUserByNamespaceId(getId(), namespaceContext)
                .map(r -> builder.build(value, r, context));
    }

    @Override
    public Stream<NamespaceUser> findUsers(NamespaceUserType type) {
        return namespaceDbHelper.findNamespaceUserByNamespaceIdAndType(getId(), type, namespaceContext)
                .map(r -> builder.build(value, r, context));
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
        Long entryId = null;
        if (accessLevel == AccessLevel.open) {
            active = true;
            approvedDate = LocalDateTime.now();
            approvedBy = context.getPrincipal();
            entryId = getBucket().addContainer(BaseCustomType.MEMBER).getId();
        }
        NamespaceUserRecord namespaceUserRecord = namespaceDbHelper.addNamespaceUser(getId(), user, user.getValue().displayName(), type,
                active, approvedDate, approvedBy, entryId, SecurityClass.U, expirationDate, context);
        return builder.build(value, namespaceUserRecord, context);
    }

    @Override
    public Optional<NamespaceUser> findUserById(Integer userId) {
        return namespaceDbHelper.findNamespaceUserByNamespaceIdAndId(getId(), userId, namespaceContext)
                .map(user -> builder.build(value, user, context));

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
                        tb.getSecurityLevel(),
                        tb.getPermissions(),
                        tb.isActive(),
                        tb.getActivationDate(),
                        tb.getExpirationDate(),
                        tb.getApprovedBy(),
                        tb.getApprovedDate(),
                        tb.getCreatedBy(),
                        tb.getCreatedDate(),
                        tb.getModifiedBy(),
                        tb.getModifiedDate(),
                        tb.getFirstName(),
                        tb.getLastName(),
                        tb.getEmail(),
                        tb.getPhone(),
                        tb.getImageUrl()
                ));
    }


}
