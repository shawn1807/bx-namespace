package com.tsu.namespace.repo;

import com.tsu.namespace.entities.EntityTypeTb;
import com.tsu.namespace.entities.id.EntityTypeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface EntityTypeRepository extends JpaRepository<EntityTypeTb, EntityTypeId> {

    Stream<EntityTypeTb> findByIdNamespaceId(UUID namespaceId);

    Optional<EntityTypeTb> findByIdNamespaceIdAndIdId(UUID namespaceId, Integer id);

    Optional<EntityTypeTb> findByIdNamespaceIdAndName(UUID namespaceId, String name);
}
