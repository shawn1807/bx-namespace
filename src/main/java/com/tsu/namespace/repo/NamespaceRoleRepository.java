package com.tsu.namespace.repo;

import com.tsu.namespace.entities.NamespaceRoleTb;
import com.tsu.namespace.entities.id.NamespaceRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface NamespaceRoleRepository extends JpaRepository<NamespaceRoleTb, NamespaceRoleId> {


    Stream<NamespaceRoleTb> findByIdNamespaceIdOrderByName(UUID namespaceId);
}
