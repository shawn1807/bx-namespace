package com.tsu.namespace.upgrades.impl;

import com.tsu.auth.security.AppSecurityContext;
import com.tsu.common.upgrades.AppUpgradeContext;
import com.tsu.entry.service.BucketService;
import com.tsu.namespace.service.AppService;
import com.tsu.namespace.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@ToString
@RequiredArgsConstructor
public class AppUpgradeContextImpl implements AppUpgradeContext {

    private final AppSecurityContext securityContext;
    private final AppService appService;
    private final UserService userService;
    private final BucketService bucketService;
    private final ApplicationContext applicationContext;
    private final PlatformTransactionManager transactionManager;

    @Override
    public AppService getAppService() {
        return appService;
    }

    @Override
    public AppSecurityContext getContext() {
        return securityContext;
    }

    @Override
    public BucketService getBucketService() {
        return bucketService;
    }

    @Override
    public UserService getUserService() {
        return userService;
    }



    @Override
    public <T> T findBean(Class<T> type) {
        return applicationContext.getBean(type);
    }

    @Override
    public <T> T findBean(String name, Class<T> type) {
        return applicationContext.getBean(name, type);
    }

    @Override
    public JdbcTemplate getJdbcTemplate(String dsBeanName) {
        return new JdbcTemplate(findBean(dsBeanName, DataSource.class));
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(findBean(DataSource.class));
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }
}

