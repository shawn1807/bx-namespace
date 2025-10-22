package com.tsu.namespace.helper;

import com.tsu.common.upgrades.AppUpgrade;
import com.tsu.common.val.AppModuleVal;
import com.tsu.namespace.entities.AppModuleTb;
import com.tsu.namespace.entities.UpgradeHistoryTb;
import com.tsu.namespace.record.AppModuleRecord;
import com.tsu.namespace.repo.AppModuleRepository;
import com.tsu.namespace.repo.UpgradeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;


@RequiredArgsConstructor
@Component
public class AppModuleDbHelper {

    private final AppModuleRepository repository;
    private final UpgradeHistoryRepository historyRepository;

    public Stream<AppModuleRecord> findAllAppModules() {
        return repository.findAll()
                .stream()
                .map(tb -> new AppModuleRecord(tb, repository::save));
    }

    public Optional<AppModuleRecord> findAppModule(String name) {
        return repository.findByName(name)
                .map(tb -> new AppModuleRecord(tb, repository::save));
    }

    public AppModuleRecord createModule(String name, String version, Integer build, String buildPackage) {
        AppModuleTb tb = new AppModuleTb();
        tb.setName(name);
        tb.setVersion(version);
        tb.setBuild(build != null ? build : 0);
        tb.setBuildPackage(buildPackage);
        return new AppModuleRecord(tb, repository::save);
    }

    public void addUpgradeHistory(AppModuleVal module, AppUpgrade build) {
        UpgradeHistoryTb upgrade = new UpgradeHistoryTb();
        upgrade.setModule(module.name());
        upgrade.setVersion(build.getModuleVersion());
        upgrade.setDescription(build.getDescription());
        upgrade.setBuild(build.getBuild());
        upgrade.setUpgradeDate(LocalDateTime.now());
        historyRepository.save(upgrade);
    }
}
