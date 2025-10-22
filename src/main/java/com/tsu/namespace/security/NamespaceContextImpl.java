package com.tsu.namespace.security;

import com.tsu.auth.api.BasePrincipal;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.auth.security.NamespaceContext;
import com.tsu.namespace.api.Namespace;
import com.tsu.namespace.api.NamespaceRole;
import com.tsu.namespace.api.NamespaceUser;

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
