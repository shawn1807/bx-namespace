package com.tsu.namespace.repo;

import com.tsu.namespace.entities.AppModuleTb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppModuleRepository extends JpaRepository<AppModuleTb, Integer> {

    Optional<AppModuleTb> findByName(String name);

}
