package com.tsu.namespace.security;

import com.tsu.base.api.Namespace;
import com.tsu.base.api.NamespaceRole;
import com.tsu.base.api.NamespaceUser;
import com.tsu.namespace.api.WorkspaceUser;
import com.tsu.namespace.api.namespace.DomainObjectBuilder;
import com.tsu.namespace.helper.WorkspaceDbHelper;
import com.tsu.common.api.BasePrincipal;
import com.tsu.security.AppSecurityContext;
import com.tsu.security.NamespaceContext;

import java.util.Optional;
import java.util.UUID;

public class NamespaceContextImpl implements NamespaceContext {

    private final AppSecurityContext context;
    private final NamespaceUser namespaceUser;

    public NamespaceContextImpl(AppSecurityContext context, NamespaceUser namespaceUser) {
        this.context = context;
        this.namespaceUser = namespaceUser;
    }


    @Override
    public AppSecurityContext getSecurityContext() {
        return context;
    }

    @Override
    public Namespace getNamespace() {
        return namespaceUser.getNamespace();
    }

    @Override
    public NamespaceUser getNamespaceUser() {
        return namespaceUser;
    }

    @Override
    public Integer getNamespaceUserId() {
        return getNamespaceUser().getId();
    }

    @Override
    public UUID getNamespaceId() {
        return namespaceUser.getNamespace().getId();
    }

    @Override
    public Optional<NamespaceRole> getNamespaceRole() {
        return getNamespaceUser().getRole();
    }

    @Override
    public BasePrincipal getPrincipal() {
        return context.getPrincipal();
    }


}
