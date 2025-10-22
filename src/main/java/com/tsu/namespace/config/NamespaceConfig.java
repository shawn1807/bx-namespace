package com.tsu.namespace.config;

import com.tsu.namespace.api.namespace.NamespaceObjectFactory;
import com.tsu.namespace.repo.EntityRepository;
import com.tsu.namespace.service.IDGeneratorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackageClasses = {EntityRepository.class})
@ComponentScan(basePackages = {"com.tsu.namespace.service", "com.tsu.namespace.helper", "com.tsu.common.upgrades"})
public class NamespaceConfig {


    @Bean
    public IDGeneratorService idGenerator() {
        return new IDGeneratorService();
    }

    @Bean
    public NamespaceObjectFactory namespaceObjectFactory() {
        return new NamespaceObjectFactory();
    }

}
