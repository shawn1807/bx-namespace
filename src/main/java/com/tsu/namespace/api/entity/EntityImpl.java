package com.tsu.namespace.api.entity;

import com.tsu.auth.permissions.NamespaceAction;
import com.tsu.entry.api.EntryTextManager;
import com.tsu.enums.BaseExceptionCode;
import com.tsu.enums.BaseParamName;
import com.tsu.namespace.api.Entity;
import com.tsu.namespace.api.Namespace;
import com.tsu.namespace.api.namespace.NamespaceObjectFactory;
import com.tsu.namespace.helper.EntityDbHelper;
import com.tsu.namespace.record.EntityRecord;
import com.tsu.namespace.record.EntityTypeRecord;
import com.tsu.place.api.Place;
import com.tsu.workspace.api.TextManager;
import com.tsu.workspace.val.EntityTypeVal;
import com.tsu.workspace.val.EntityVal;
import com.tsu.common.api.ActionPack;
import com.tsu.common.exception.UserException;
import com.tsu.common.utils.LazyCacheLoader;
import com.tsu.common.utils.ParamValidator;
import com.tsu.common.vo.Email;
import com.tsu.common.vo.Text;
import com.tsu.entry.api.Node;
import com.tsu.auth.security.NamespaceContext;
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
    private final NamespaceContext context;
    private final NamespaceObjectFactory factory;
    private final LazyCacheLoader<TextManager> textManager;

    public EntityImpl(Namespace namespace, EntityRecord record, EntityDbHelper dbHelper,
                      NamespaceContext context, NamespaceObjectFactory factory) {
        this.namespace = namespace;
        this.record = record;
        this.dbHelper = dbHelper;
        this.context = context;
        this.factory = factory;
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
                .map(e -> factory.build(namespace, e, this.context));
    }


    @Override
    public EntityVal getValue() {
        return record.getValue();
    }


}
