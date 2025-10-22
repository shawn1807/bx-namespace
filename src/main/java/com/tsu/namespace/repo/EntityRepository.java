package com.tsu.namespace.repo;

import com.tsu.namespace.entities.EntityTb;
import com.tsu.base.api.EntityType;
import com.tsu.namespace.entities.EntityTypeTb;
import com.tsu.namespace.entities.id.EntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface EntityRepository extends JpaRepository<EntityTb, EntityId> {

    Optional<EntityTb> findByIdNamespaceIdAndNameAndTypeId(UUID namespaceId, String name, Integer entityTypeId);

    Optional<EntityTb> findByIdNamespaceIdAndIdId(UUID namespaceId, UUID entityId);


}
