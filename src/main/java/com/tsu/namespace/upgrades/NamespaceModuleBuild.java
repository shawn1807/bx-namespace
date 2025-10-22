package com.tsu.namespace.upgrades;

import com.tsu.common.upgrades.AppUpgrade;

abstract class NamespaceModuleBuild implements AppUpgrade {


    @Override
    public String getModuleVersion() {
        return "2025";
    }
}
