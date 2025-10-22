package com.tsu.namespace.api.manager;

import com.tsu.namespace.api.CalendarManager;
import com.tsu.namespace.api.Namespace;
import com.tsu.auth.security.AppSecurityContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NamespaceCalendarManager implements CalendarManager {

    private final Namespace namespace;
    private final AppSecurityContext context;

    public NamespaceCalendarManager(Namespace namespace, AppSecurityContext context) {
        this.namespace = namespace;
        this.context = context;
    }
}
