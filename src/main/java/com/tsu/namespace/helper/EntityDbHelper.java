package com.tsu.namespace.helper;

import com.tsu.namespace.entities.EntityTb;
import com.tsu.namespace.entities.EntityTypeTb;
import com.tsu.namespace.entities.id.EntityId;
import com.tsu.namespace.entities.id.EntityTypeId;
import com.tsu.namespace.record.EntityRecord;
import com.tsu.namespace.record.EntityTypeRecord;
import com.tsu.namespace.repo.EntityRepository;
import com.tsu.namespace.repo.EntityTypeRepository;
import com.tsu.common.jpa.JsonValueUtils;
import com.tsu.auth.security.NamespaceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class EntityDbHelper {

    private final EntityTypeRepository entityTypeRepository;
    private final EntityRepository entityRepository;


    public Stream<EntityTypeRecord> findEntityTypesByNamespaceId(UUID namespaceId) {
        return entityTypeRepository.findByIdNamespaceId(namespaceId)
                .map(tb -> new EntityTypeRecord(tb, entityTypeRepository::save));
    }

    public Optional<EntityTypeRecord> findEntityTypeByNamespaceIdAndTypeId(UUID namespaceId, Integer typeId) {
        return entityTypeRepository.findByIdNamespaceIdAndIdId(namespaceId, typeId)
                .map(tb -> new EntityTypeRecord(tb, entityTypeRepository::save));
    }

    public Optional<EntityTypeRecord> findEntityTypeByNamespaceIdAndType(UUID namespaceId, String name) {
        return entityTypeRepository.findByIdNamespaceIdAndName(namespaceId, name)
                .map(tb -> new EntityTypeRecord(tb, entityTypeRepository::save));
    }

    public Optional<EntityRecord> findEntityByNamespaceIdAndNameAndTypeId(UUID namespaceId, String name, Integer typeId,NamespaceContext context) {
        return entityRepository.findByIdNamespaceIdAndNameAndTypeId(namespaceId, name, typeId)
                .map(tb -> buildEntityRecord(tb, context));
    }

    private EntityRecord buildEntityRecord(EntityTb tb, NamespaceContext context) {
        return new EntityRecord(tb, e -> {
            e.setModifiedBy(context.getNamespaceUserId());
            e.setModifiedDate(LocalDateTime.now());
            entityRepository.save(e);
        });
    }

    public Optional<EntityRecord> findEntityByNamespaceIdAndId(UUID namespaceId, UUID entityId, NamespaceContext context) {
        return entityRepository.findByIdNamespaceIdAndIdId(namespaceId, entityId)
                .map(tb -> new EntityRecord(tb, e -> {
                    e.setModifiedBy(context.getNamespaceUserId());
                    e.setModifiedDate(LocalDateTime.now());
                    entityRepository.save(e);
                }));
    }

    public EntityTypeRecord addEntityType(UUID namespaceId, String name, NamespaceContext context) {
        EntityTypeTb type = new EntityTypeTb();
        type.setId(new EntityTypeId(namespaceId, null));
        type.setName(name);
        type.setCreatedBy(context.getNamespaceUserId());
        type.setCreatedDate(LocalDateTime.now());
        entityTypeRepository.save(type);
        return new EntityTypeRecord(type, entityTypeRepository::save);
    }

    public EntityRecord addEntity(UUID id, UUID namespaceId, EntityTypeRecord type, String name, String email,String phone, Object profile,
                                  UUID parentId, Integer placeId, NamespaceContext context) {
        EntityTb e = new EntityTb();
        e.setId(new EntityId(namespaceId, id));
        e.setTypeId(type.getId());
        e.setName(name);
        e.setEmail(email);
        e.setPhone(phone);
        e.setActive(true);
        e.setProfile(JsonValueUtils.getInstance().encodeAsJson(profile));
        e.setCreatedBy(context.getNamespaceUserId());
        e.setCreatedDate(LocalDateTime.now());
        e.setModifiedBy(context.getNamespaceUserId());
        e.setModifiedDate(LocalDateTime.now());
        e.setPrimaryPlaceId(placeId);
        e.setParentId(parentId);
        entityRepository.save(e);
        return buildEntityRecord(e, context);
    }
}
