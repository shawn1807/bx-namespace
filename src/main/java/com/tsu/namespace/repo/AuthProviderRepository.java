package com.tsu.namespace.repo;

import com.tsu.namespace.entities.AuthProviderTb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthProviderRepository extends JpaRepository<AuthProviderTb, Integer> {


    Optional<AuthProviderTb> findByName(String name);


}
