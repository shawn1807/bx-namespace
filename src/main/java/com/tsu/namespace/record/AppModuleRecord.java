package com.tsu.namespace.record;

import com.tsu.common.val.AppModuleVal;
import com.tsu.namespace.entities.AppModuleTb;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.function.Consumer;


@RequiredArgsConstructor
public class AppModuleRecord {

    private final AppModuleTb tb;
    private final Consumer<AppModuleTb> save;


    public void persist() {
        tb.setModifiedDate(LocalDateTime.now());
        save.accept(tb);
    }

    public AppModuleVal getValue() {
        return new AppModuleVal(tb.getName(), tb.getVersion(), tb.getBuildPackage(), tb.getBuild(), tb.getPriority());
    }

    public String getName() {
        return tb.getName();
    }

    public String getVersion() {
        return tb.getVersion();
    }

    public String getBuildPackage() {
        return tb.getBuildPackage();
    }

    public int getBuild() {
        return tb.getBuild();
    }

    public int getPriority() {
        return tb.getPriority();
    }

    public void setVersion(String version) {
        tb.setVersion(version);
    }

    public void setBuild(int build) {
        tb.setBuild(build);
    }
}
