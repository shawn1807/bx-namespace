package com.tsu.namespace.security;

import com.tsu.namespace.api.AuthLogin;
import com.tsu.base.api.UserBase;
import com.tsu.namespace.api.namespace.DomainObjectBuilder;
import com.tsu.base.enums.BaseConstants;
import com.tsu.namespace.helper.NamespaceDbHelper;
import com.tsu.namespace.record.NamespaceRecord;
import com.tsu.base.service.IDGeneratorService;
import com.tsu.base.val.NamespaceVal;
import com.tsu.common.api.BasePrincipal;
import com.tsu.entry.api.BucketContext;
import com.tsu.entry.service.BucketService;
import com.tsu.security.AppSecurityContext;
import com.tsu.security.AppSecurityContextInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class AdminContextInitializer implements AppSecurityContextInitializer {

    @Autowired
    private IDGeneratorService idGenerator;
    @Autowired
    private BucketService bucketService;
    @Autowired
    private NamespaceDbHelper namespaceDbHelper;
    @Autowired
    private DomainObjectBuilder builder;


    private com.tsu.base.security.AdminSecurityContext _adminContext;

    public AdminContextInitializer() {
    }

    @Override
    public AppSecurityContext initializeAndVerify() {
        return Optional.ofNullable(this._adminContext)
                .orElseGet(() -> {
                    BucketContext adminStorageContext = bucketService.getAdminBucketContext();
                    this._adminContext = new AdminSecurityContextImpl(namespaceDbHelper, adminStorageContext, idGenerator);
                    return this._adminContext;
                });
    }

    @Override
    public AuthLogin getContextAuthLogin() {
        throw new IllegalStateException("getAuthLogin not support");
    }


    @Override
    public boolean isUserRegistered() {
        return true;
    }


    private final class AdminSecurityContextImpl implements AdminSecurityContext {

        private final BucketContext storageContext;
        private final String txid;
        private final NamespaceDbHelper namespaceDbHelper;

        public AdminSecurityContextImpl(NamespaceDbHelper namespaceDbHelper, BucketContext adminContext, IDGeneratorService idGenerator) {
            this.namespaceDbHelper = namespaceDbHelper;
            this.storageContext = adminContext;
            this.txid = idGenerator.nextUUID().toString();
        }

        @Override
        public BasePrincipal getPrincipal() {
            return BaseConstants.SYSADMIN;
        }

        @Override
        public String getName() {
            return "SYSADMIN";
        }


        @Override
        public BucketContext getBucketContext() {
            return storageContext;
        }

        @Override
        public String getTxid() {
            return txid;
        }

        @Override
        public List<GrantedAuthority> getGranAuthorities() {
            return List.of();
        }

        @Override
        public Optional<UserBase> getUser() {
            return Optional.empty();
        }


        private final static Authentication adminAuthentication = new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.emptyList();
            }

            @Override
            public Object getCredentials() {
                return this;
            }

            @Override
            public Object getDetails() {
                return this;
            }

            @Override
            public Object getPrincipal() {
                return this;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
                throw new IllegalArgumentException("method not supported");
            }

            @Override
            public String getName() {
                return BaseConstants.SYSADMIN.id().toString();
            }
        };
    }


}
