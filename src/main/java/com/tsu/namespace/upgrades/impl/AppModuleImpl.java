package com.tsu.namespace.upgrades.impl;

import com.google.common.reflect.ClassPath;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.common.upgrades.AppModule;
import com.tsu.common.upgrades.AppUpgrade;
import com.tsu.common.upgrades.AppUpgradeContext;
import com.tsu.common.val.AppModuleVal;
import com.tsu.entry.service.BucketService;
import com.tsu.namespace.helper.AppModuleDbHelper;
import com.tsu.namespace.record.AppModuleRecord;
import com.tsu.namespace.service.AppService;
import com.tsu.namespace.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AppModuleImpl implements AppModule {

    private final AppModuleRecord record;
    private final AppModuleDbHelper dbHelper;
    private final AppService appService;
    private final BucketService bucketService;
    private final UserService userService;
    private final ApplicationContext applicationContext;
    private final PlatformTransactionManager transactionManager;
    private final AppSecurityContext context;


    private List<AppUpgrade> loadUpgradeFiles() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            log.info("load upgrade files from package: {}", record.getBuildPackage());
            return ClassPath.from(loader)
                    .getTopLevelClasses(record.getBuildPackage())
                    .stream()
                    .map(c-> {
                        try {
                            return Class.forName(c.getName());
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(clz -> !clz.isInterface())
                    .filter(clz-> !Modifier.isAbstract(clz.getModifiers()))
                    .peek(i-> log.debug("load class: {}", i.getName()))
                    .filter(AppUpgrade.class::isAssignableFrom)
                    .map(clazz -> {
                        try {
                            return (AppUpgrade) clazz.getDeclaredConstructor().newInstance();
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(build -> build.getBuild() > record.getBuild())
                    .sorted(Comparator.comparing(AppUpgrade::getBuild))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AppModuleVal getValue() {
        return record.getValue();
    }

    @Override
    public void upgrade(ApplicationArguments args) {
        List<AppUpgrade> upgrades = loadUpgradeFiles();
        if (upgrades.isEmpty()) {
            log.info("No upgrades for module: {}", record.getName());
        } else {
            AppUpgradeContext upgradeContext = new AppUpgradeContextImpl(context, appService, userService,bucketService, applicationContext, transactionManager);
            TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
            transactionTemplate.execute(status -> {
                upgrades.forEach(build -> {
                    log.info("upgrade build({}): {}", build.getName(), build.getDescription());
                    build.execute(upgradeContext);
                    dbHelper.addUpgradeHistory(record.getValue(), build);
                    record.setBuild(build.getBuild());
                    record.setVersion(build.getModuleVersion());
                    record.persist();
                });
                return record;
            });
        }
    }
}
