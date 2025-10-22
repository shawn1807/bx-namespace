package com.tsu.namespace.security;

import com.tsu.auth.security.AppSecurityContext;
import com.tsu.namespace.api.Namespace;

public interface NamespaceLookup {


    Namespace lookup(String namespaceId,AppSecurityContext context);

}
