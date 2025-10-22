package com.tsu.namespace.config;

import com.tsu.namespace.api.namespace.DomainObjectBuilder;
import com.tsu.namespace.repo.EntityRepository;
import com.tsu.base.service.IDGeneratorService;
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
    public DomainObjectBuilder namespaceObjectBuilder() {
        return new DomainObjectBuilder();
    }

    // Event manager bean removed - workspace/work related beans will be in bx-base module
}
