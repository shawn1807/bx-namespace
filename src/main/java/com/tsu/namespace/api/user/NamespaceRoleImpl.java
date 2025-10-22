package com.tsu.namespace.api.user;

import com.tsu.base.api.Namespace;
import com.tsu.base.api.NamespaceRole;
import com.tsu.base.enums.BaseConstants;
import com.tsu.base.enums.NamespaceAction;
import com.tsu.namespace.record.NamespaceRoleRecord;
import com.tsu.namespace.util.Permissions;
import com.tsu.base.val.NamespaceRoleVal;
import com.tsu.base.val.PermissionVal;
import com.tsu.common.api.ActionPack;
import com.tsu.common.api.Permission;
import com.tsu.common.api.PermissionEffect;
import com.tsu.common.utils.LazyCacheLoader;
import com.tsu.security.NamespaceContext;

import java.util.List;
import java.util.Optional;

public class NamespaceRoleImpl implements NamespaceRole {

    private final NamespaceRoleRecord record;
    private final NamespaceContext context;
    private final LazyCacheLoader<Permissions> helper;

    public NamespaceRoleImpl(NamespaceRoleRecord record,NamespaceContext context) {
        this.record = record;
        this.context = context;
        this.helper = LazyCacheLoader.of(() -> new Permissions(record.getPermissions(BaseConstants.PERMISSION_LIST_TYPE)));
    }

    @Override
    public Namespace getNamespace() {
        return context.getNamespace();
    }

    @Override
    public NamespaceRoleVal getValue() {
        return record.getValue();
    }

    @Override
    public List<PermissionVal> getPermissions() {
        return helper.get().findPermissions();
    }

    @Override
    public void setPermission(Permission permission, PermissionEffect effect) {
        context.getNamespace().getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.MANAGER_ROLE));
        helper.get().setPermission(permission, effect);
    }

    @Override
    public Optional<PermissionVal> getPermission(Permission permission) {
        return helper.get().findPermission(permission);
    }

    @Override
    public void removePermission(Permission permission) {
        context.getNamespace().getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.MANAGER_ROLE));
        helper.get().removePermission(permission);
    }

    @Override
    public void setPermissions(List<Permission> permissions, PermissionEffect effect) {
        context.getNamespace().getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.MANAGER_ROLE));
        helper.get().setPermissions(permissions, effect);
    }


}
