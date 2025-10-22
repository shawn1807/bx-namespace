package com.tsu.namespace.api.entity;

import com.tsu.namespace.api.*;
import com.tsu.namespace.api.manager.EntryTextManager;
import com.tsu.namespace.api.namespace.DomainObjectBuilder;
import com.tsu.base.enums.*;
import com.tsu.namespace.helper.EntityDbHelper;
import com.tsu.namespace.helper.WorkspaceDbHelper;
import com.tsu.namespace.record.EntityRecord;
import com.tsu.namespace.record.EntityTypeRecord;
import com.tsu.namespace.record.WorkspaceRecord;
import com.tsu.namespace.record.WorkspaceTypeRecord;
import com.tsu.base.val.EntityTypeVal;
import com.tsu.base.val.EntityVal;
import com.tsu.common.api.ActionPack;
import com.tsu.common.exception.UserException;
import com.tsu.common.utils.LazyCacheLoader;
import com.tsu.common.utils.ParamValidator;
import com.tsu.common.vo.Email;
import com.tsu.common.vo.Text;
import com.tsu.entry.api.Node;
import com.tsu.security.AppSecurityContext;
import com.tsu.security.NamespaceContext;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EntityImpl implements Entity {

    private final Namespace namespace;
    @EqualsAndHashCode.Include
    private final EntityRecord record;
    private final EntityDbHelper dbHelper;
    private final WorkspaceDbHelper workspaceDbHelper;
    private final NamespaceContext context;
    private final DomainObjectBuilder builder;
    private final LazyCacheLoader<TextManager> textManager;

    public EntityImpl(Namespace namespace, EntityRecord record, EntityDbHelper dbHelper,
                      WorkspaceDbHelper workspaceDbHelper, NamespaceContext context, DomainObjectBuilder builder) {
        this.namespace = namespace;
        this.record = record;
        this.dbHelper = dbHelper;
        this.workspaceDbHelper = workspaceDbHelper;
        this.context = context;
        this.builder = builder;
        this.textManager = LazyCacheLoader.of(() -> new EntryTextManager(getNode(), namespace.getPermissionManager()));
    }

    @Override
    public TextManager getTextManager() {
        return textManager.get();
    }

    @Override
    public UUID getId() {
        return record.getId();
    }

    @Override
    public void update(Text name, Email email,String phone) {
        ParamValidator.builder()
                .withNonNullOrEmpty(name, BaseParamName.NAME)
                .withNonNull(email, BaseParamName.EMAIL)
                .withVerifyEmail(email)
                .throwIfErrors();
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.UPDATE_ENTITY, Map.of("name", name, "email", email)));
        String entityName = name.strip();
        String entityEmail = email.validateAndGet();
        record.setName(entityName);
        record.setEmail(entityEmail);
        record.setPhone(phone);
        record.persist();
    }


    @Override
    public EntityTypeVal getType() {
        return dbHelper.findEntityTypeByNamespaceIdAndTypeId(namespace.getId(), record.getEntityTypeId())
                .map(EntityTypeRecord::getValue)
                .orElseThrow(() -> new UserException(BaseExceptionCode.ENTITY_TYPE_NOT_EXISTS, Map.of(BaseParamName.ENTITY_TYPE, record.getEntityTypeId())));
    }

    @Override
    public Namespace getNamespace() {
        return namespace;
    }

    @Override
    public Workspace getPrimaryWorkspace() {
        WorkspaceManager manager = namespace.getWorkspaceManager();
        return manager.findWorkspaceById(getId())
                .orElseThrow(()-> new UserException(BaseExceptionCode.MISSING_WORKSPACE));
    }

    @Override
    public Optional<Place> getPrimaryPlace() {
        return Optional.ofNullable(record.getPrimaryPlaceId())
                .flatMap(namespace.getPlaceManager()::findPlaceById);
    }

    @Override
    public <T> Optional<T> getProfile(Class<T> type) {
        return Optional.ofNullable(record.getProfile(type));
    }

    @Override
    public void setProfile(Object profile) {
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.UPDATE_ENTITY_PROFILE, profile));
        record.setProfile(profile);
        record.persist();
    }


    @Override
    public boolean isActive() {
        return record.isActive();
    }


    @Override
    public void activate() {
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.ACTIVATE_ENTITY));
        record.setActive(true);
        record.persist();
    }

    @Override
    public void deactivate() {
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.DEACTIVATE_ENTITY));
        record.setActive(false);
        record.persist();
    }

    @Override
    public Node getNode() {
        return namespace.getBucket().findNode(record::getId)
                .orElseThrow(() -> new IllegalStateException("entity node not exists: " + record.getId()));
    }


    @Override
    public Optional<Entity> getParent() {
        return Optional.ofNullable(record.getParentId())//
                .flatMap(pid -> dbHelper.findEntityByNamespaceIdAndId(namespace.getId(), pid,context))//
                .map(e -> builder.build(namespace, e, this.context));
    }


    @Override
    public EntityVal getValue() {
        return record.getValue();
    }


}
