package com.tsu.namespace.security;

import com.tsu.auth.api.BasePrincipal;
import com.tsu.common.utils.LazyCacheLoader;
import com.tsu.entry.api.BucketContext;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.namespace.api.UserBase;
import com.tsu.auth.security.AppJwtAuthenticationToken;
import com.tsu.namespace.api.namespace.NamespaceObjectFactory;
import com.tsu.namespace.record.LoginRecord;
import com.tsu.namespace.record.UserRecord;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class WebAppSecurityContext implements AppSecurityContext {

    private final AppJwtAuthenticationToken authenticationToken;
    private final LazyCacheLoader<BucketContext> bucketContext = LazyCacheLoader.of((BucketContextImpl::new));
    private final LoginRecord loginRecord;
    private final UserBase userBase;
    private final String txid;
    private final LazyCacheLoader<List<GrantedAuthority>> authorities;
    private final HttpServletRequest request;

    public WebAppSecurityContext(HttpServletRequest request, UserRecord userRecord, LoginRecord loginRecord,
                                 AppJwtAuthenticationToken authenticationToken, String txid, NamespaceObjectFactory factory) {
        this.request = request;
        this.loginRecord = loginRecord;
        this.userBase = factory.build(userRecord, this);
        this.authenticationToken = authenticationToken;
        this.txid = txid;
        this.authorities = LazyCacheLoader.of(() -> authenticationToken.getAuthorities() //
                .stream()//
                .map(g -> (GrantedAuthority) g)
                .toList()
        );
    }

    class BucketContextImpl implements BucketContext {


        @Override
        public BasePrincipal getUser() {
            return getPrincipal();
        }

        @Override
        public String getTxId() {
            return txid;
        }

        @Override
        public List<GrantedAuthority> getGrantedAuthorities() {
            return Stream.ofNullable(authenticationToken.getAuthorities())
                    .flatMap(Collection::stream)
                    .toList();

        }

    }


    @Override
    public BasePrincipal getPrincipal() {
        return loginRecord.getUserId();
    }

    @Override
    public String getName() {
        return userBase.getValue().displayName();
    }


    @Override
    public BucketContext getBucketContext() {
        return bucketContext.get();
    }

    @Override
    public String getTxid() {
        return txid;
    }

    @Override
    public List<GrantedAuthority> getGranAuthorities() {
        return authorities.get();
    }

    @Override
    public Optional<UserBase> getUser() {
        return Optional.of(userBase);
    }


}
