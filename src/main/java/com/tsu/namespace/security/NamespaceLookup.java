package com.tsu.namespace.security;

import com.tsu.base.api.Namespace;
import com.tsu.security.AppSecurityContext;

public interface NamespaceLookup {


    Namespace lookup(String namespaceId,AppSecurityContext context);

}
