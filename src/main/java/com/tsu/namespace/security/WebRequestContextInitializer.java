package com.tsu.namespace.security;

import com.tsu.namespace.api.AuthLogin;
import com.tsu.namespace.api.namespace.DomainObjectBuilder;
import com.tsu.base.enums.AuthProvider;
import com.tsu.base.enums.BaseExceptionCode;
import com.tsu.namespace.helper.UserDbHelper;
import com.tsu.namespace.record.LoginRecord;
import com.tsu.namespace.record.UserRecord;
import com.tsu.base.service.IDGeneratorService;
import com.tsu.common.exception.UserException;
import com.tsu.common.utils.LazyCacheLoader;
import com.tsu.security.AppSecurityContext;
import com.tsu.security.AppSecurityContextInitializer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
public class WebRequestContextInitializer implements AppSecurityContextInitializer {


    @Autowired
    private UserDbHelper userDbHelper;

    @Autowired
    private DomainObjectBuilder builder;

    @Autowired
    private IDGeneratorService idGenerator;

    private final HttpServletRequest request;

    private final LazyCacheLoader<AppSecurityContext> context;
    private final LazyCacheLoader<LoginRecord> login;


    public WebRequestContextInitializer(HttpServletRequest request) {
        this.request = request;
        this.context = LazyCacheLoader.of(() -> Optional.of(SecurityContextHolder.getContext())//
                        .map(SecurityContext::getAuthentication)//
                        .map(this::verifyAndGet)//
                        .orElseThrow(() -> new UserException(BaseExceptionCode.NOT_AUTHENTICATED)),
                () -> log.info("context initialized")
        );
        this.login = LazyCacheLoader.of(() -> Optional.of(SecurityContextHolder.getContext())//
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    AuthProvider authProvider = getAuthProviderFromToken(auth);
                    return userDbHelper.findLogins(authProvider, auth.getName());
                })
                .orElse(null));
    }


    @Override
    public boolean isUserRegistered() {
        return login.getAsOptional()
                .isPresent();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public AppSecurityContext initializeAndVerify() {
        return this.context.get();
    }

    @Override
    public AuthLogin getContextAuthLogin() {
        return login.get().getAuthLogin();
    }


    private AppSecurityContext verifyAndGet(Authentication authentication) {
        if (authentication instanceof AppJwtAuthenticationToken token) {
            UserRecord userRecord = login.getAsOptional()
                    .filter(LoginRecord::isActive)
                    .flatMap(login -> userDbHelper.findUserById(login.getUserId().id()))
                    .filter(s -> s.getExpirationDate() == null || LocalDate.now().isBefore(s.getExpirationDate()))
                    .orElseThrow(() -> new UserException(BaseExceptionCode.INACTIVE_ACCOUNT));
            String txid = idGenerator.nextUUID().toString();
            return new WebAppSecurityContext(request, userRecord, login.get(), token, txid, builder);
        }
        throw new IllegalStateException("Authentication not supported: " + authentication);
    }

    private AuthProvider getAuthProviderFromToken(Authentication authentication) {
        if (authentication instanceof AppJwtAuthenticationToken token) {
            return token.getAuthProvider();
        }
        throw new IllegalStateException("Authentication is not supported");
    }

}
