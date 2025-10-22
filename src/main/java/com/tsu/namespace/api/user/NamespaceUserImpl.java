package com.tsu.namespace.api.user;

import com.tsu.auth.api.Permission;
import com.tsu.auth.api.PermissionEffect;
import com.tsu.auth.permissions.NamespaceAction;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.common.api.ActionPack;
import com.tsu.common.utils.LazyCacheLoader;
import com.tsu.common.val.PermissionVal;
import com.tsu.enums.BaseConstants;
import com.tsu.namespace.api.*;
import com.tsu.namespace.api.namespace.NamespaceObjectFactory;
import com.tsu.namespace.helper.UserDbHelper;
import com.tsu.namespace.record.NamespaceRecord;
import com.tsu.namespace.record.NamespaceUserRecord;
import com.tsu.namespace.security.NamespaceContextImpl;
import com.tsu.namespace.val.NamespaceUserVal;
import com.tsu.util.Permissions;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class NamespaceUserImpl implements NamespaceUser {

    private final NamespaceUserRecord record;
    private final AppSecurityContext context;
    private final UserDbHelper userDbHelper;
    private final NamespaceObjectFactory factory;
    private final LazyCacheLoader<Permissions> helper;
    private final Namespace namespace;
    private final LazyCacheLoader<NamespaceRole> role;


    public NamespaceUserImpl(NamespaceRecord namespace, NamespaceUserRecord record, AppSecurityContext context,
                             UserDbHelper userDbHelper, NamespaceObjectFactory factory) {
        this.record = record;
        this.context = context;
        this.userDbHelper = userDbHelper;
        this.factory = factory;
        this.helper = LazyCacheLoader.of(() -> new Permissions(record.getPermissions(BaseConstants.PERMISSION_LIST_TYPE)));
        this.namespace = factory.build(namespace, this, context);
        this.role = LazyCacheLoader.of(() -> record.getRole()
                .map(r -> factory.build(r, new NamespaceContextImpl(context, this)))
                .orElse(null)
        );
    }

    public NamespaceUserImpl(Namespace namespace, NamespaceUserRecord record, AppSecurityContext context, UserDbHelper userDbHelper, NamespaceObjectFactory factory) {
        this.record = record;
        this.context = context;
        this.userDbHelper = userDbHelper;
        this.factory = factory;
        this.helper = LazyCacheLoader.of(() -> new Permissions(record.getPermissions(BaseConstants.PERMISSION_LIST_TYPE)));
        this.namespace = namespace;
        this.role = LazyCacheLoader.of(() -> record.getRole()
                .map(r -> factory.build(r, new NamespaceContextImpl(context, this)))
                .orElse(null)
        );
    }

    @Override
    public Integer getId() {
        return record.getId();
    }

    @Override
    public Namespace getNamespace() {
        return namespace;
    }

    @Override
    public UserBase getUserBase() {
        if (Objects.equals(record.getUser().id(), context.getPrincipal().id())) {
            return context.getUser().orElseThrow(() -> new IllegalStateException("User not found: " + record.getUser().id()));

        }
        return userDbHelper.findUserById(record.getUser().id())
                .map(user -> factory.build(user, context))
                .orElseThrow(() -> new IllegalStateException("User not found: " + record.getUser().id()));
    }


    @Override
    public boolean isValid() {
        if (record.isActive()) {
            return Optional.ofNullable(record.getExpirationDate())
                    .map(expiry -> LocalDate.now().isAfter(expiry))
                    .orElse(true);
        }
        return false;
    }

    @Override
    public void expire(LocalDate expiryOn) {
        getNamespace().getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.MANAGE_USER_SUBSCRIPTION));
        record.setExpirationDate(expiryOn);
        record.persist();
    }

    @Override
    public void deactivate() {
        getNamespace().getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.MANAGE_USER_SUBSCRIPTION));
        record.setActive(false);
        record.setExpirationDate(LocalDate.now());
        record.persist();
    }

    @Override
    public void activate() {
        getNamespace().getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.MANAGE_USER_SUBSCRIPTION));
        record.setActive(true);
        record.setExpirationDate(null);
        record.persist();

    }

    @Override
    public NamespaceUserType getType() {
        return record.getType();
    }

    @Override
    public void approve() {
        getNamespace().getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.MANAGE_USER_SUBSCRIPTION));
        record.setActive(true);
        record.setApprovedDate(LocalDateTime.now());
        record.setApprovedBy(context.getPrincipal());
        record.persist();
    }

    @Override
    public List<PermissionVal> getPermissions() {
        return helper.get().findPermissions();
    }

    @Override
    public Optional<NamespaceRole> getRole() {
        return role.getAsOptional();

    }

    @Override
    public NamespaceUserVal getValue() {
        return record.getValue();
    }

    @Override
    public void setPermission(Permission permission, PermissionEffect effect) {
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.MANAGER_ROLE));
        helper.get().setPermission(permission, effect);
    }

    @Override
    public Optional<PermissionVal> getPermission(Permission permission) {
        return helper.get().findPermission(permission);
    }

    @Override
    public void removePermission(Permission permission) {
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.MANAGER_ROLE));
        helper.get().removePermission(permission);
    }

    @Override
    public void setPermissions(List<Permission> permissions, PermissionEffect effect) {
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.MANAGER_ROLE));
        helper.get().setPermissions(permissions, effect);
    }



}
