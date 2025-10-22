package com.tsu.namespace.api.manager;

import com.tsu.namespace.api.Document;
import com.tsu.base.api.Namespace;
import com.tsu.base.api.NamespaceUser;
import com.tsu.namespace.api.PermissionManager;
import com.tsu.namespace.api.namespace.DomainObjectBuilder;
import com.tsu.namespace.helper.AppDbHelper;
import com.tsu.namespace.util.Permissions;
import com.tsu.base.val.NamespaceVal;
import com.tsu.base.val.PermissionVal;
import com.tsu.common.api.ActionPack;
import com.tsu.common.api.BasePrincipal;
import com.tsu.common.api.Permission;
import com.tsu.common.exception.PermissionDeniedException;
import com.tsu.security.NamespaceContext;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class NamespacePermissionManager implements PermissionManager {

    private final Namespace namespace;
    private final NamespaceContext context;
    private final AppDbHelper appDbHelper;
    private final Map<UUID, Permissions> permissions;

    public NamespacePermissionManager(NamespaceContext context,
                                      AppDbHelper appDbHelper, DomainObjectBuilder builder
    ) {
        this.namespace = context.getNamespace();
        this.context = context;
        this.appDbHelper = appDbHelper;
        this.permissions = new HashMap<>();
    }


    private List<PermissionVal> findNamespacePermissions(BasePrincipal base) {
        return permissions.computeIfAbsent(base.id(), uuid -> {
            Permissions permissions = new Permissions();
            namespace.findUser(BasePrincipal.of(uuid))
                    .filter(NamespaceUser::isValid)
                    .ifPresent(user -> {
                        user.getRole()
                                .ifPresent(role -> permissions.setPermissions(role.getPermissions()));
                        permissions.setPermissions(user.getPermissions());
                    });
            return permissions;
        }).findPermissions();
    }


    private boolean isAllowed(Permission permission, List<PermissionVal> userPermissions) {
        boolean isAllowed = userPermissions
                .stream()
                .filter(allowed -> Objects.equals(permission.getName(), allowed.permission()))
                .map(PermissionVal::isAllow)
                .findFirst()
                .orElse(false);
        if (!isAllowed) {
            log.warn("Permission not allowed: {}", permission);
        }
        return isAllowed;
    }


    @Override
    public boolean allow(BasePrincipal user, Permission... permissions) {
        NamespaceVal val = namespace.getValue();
        if (Objects.equals(val.owner().id(), user.id())) {
            return true;
        }
        List<PermissionVal> grantedPermissions = findNamespacePermissions(user);
        return Arrays.stream(permissions)
                .allMatch(permission -> isAllowed(permission, grantedPermissions));
    }

    @Override
    public boolean allow(Permission... permissions) {
        return allow(context.getPrincipal(), permissions);
    }

    @Override
    public boolean allowView(BasePrincipal user) {
        return namespace.findUser(user)
                .map(NamespaceUser::isValid)
                .orElse(false);
    }

    @Override
    public boolean allowViewDocument(Document document) {
        return false;
    }


    @Override
    public void auditAndCheckPermission(ActionPack action) {
        appDbHelper.audit(namespace.getId(), namespace.getNode().getId(), action, context);
        if (!allow(context.getPrincipal(), action.action().getRequiredPermissions().toArray(Permission[]::new))) {
            throw new PermissionDeniedException("Permission denied");
        }
    }


}
