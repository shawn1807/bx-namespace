package com.tsu.namespace.repo;

import com.tsu.namespace.entities.BasePrincipalTb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface BasePrincipalRepository extends JpaRepository<BasePrincipalTb, UUID> {


}
