package com.tsu.namespace.service.impl;

import com.tsu.common.vo.Email;
import com.tsu.common.vo.Text;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.auth.api.AuthLogin;
import com.tsu.auth.api.Login;
import com.tsu.namespace.api.UserProfile;
import com.tsu.auth.api.AuthProvider;
import com.tsu.auth.provider.AuthProviderService;
import com.tsu.namespace.api.user.LoginImpl;
import com.tsu.namespace.helper.UserDbHelper;
import com.tsu.namespace.record.LoginRecord;
import com.tsu.namespace.service.LoginService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class LoginServiceImpl implements LoginService {

    private final UserDbHelper dbHelper;
    private final Map<AuthProvider, AuthProviderService> providers;

    public LoginServiceImpl(UserDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.providers = new HashMap<>();
    }

    public void register(AuthProvider provider, AuthProviderService service) {
        log.info("Registering auth provider service: {}", provider);
        this.providers.put(provider, service);
        log.debug("Auth provider {} registered successfully. Total providers: {}", provider, providers.size());
    }


    @Override
    public Login createLogin(AuthProvider provider, UUID userId, Email email, Text password, UserProfile profile) {
        log.debug("Creating login for user: {} with provider: {}", userId, provider);
        return createLogin(provider, userId, email, password, profile, userId);
    }

    @Override
    public Login createLogin(AuthProvider provider, UUID userId, Email email, Text password, UserProfile profile, AppSecurityContext context) {
        log.debug("Creating login with security context for user: {} with provider: {}", userId, provider);
        return createLogin(provider, userId, email, password, profile, context.getPrincipal().id());
    }


    private Login createLogin(AuthProvider provider, UUID userId, Email email, Text password, UserProfile profile, UUID createdBy) {
        log.info("Creating login for user: {} with provider: {} and email: {}", userId, provider, email.validateAndGet());
        AuthProviderService service = providers.get(provider);
        if (service == null) {
            log.error("AuthProvider not supported: {}", provider);
            throw new IllegalStateException("AuthProvider not supported: " + provider);
        }
        log.debug("Found auth provider service for: {}, adding user to external system", provider);
        String authId = service.addUser(email, password, profile);
        log.debug("User added to external auth system with ID: {}, creating login record", authId);
        LoginRecord loginRecord = dbHelper.addLogin(userId, new AuthLogin(provider, authId), profile, createdBy);
        log.info("Login created successfully for user: {} with auth ID: {}", userId, authId);
        return new LoginImpl(loginRecord);
    }
}
