package com.tsu.namespace.api.manager;

import com.tsu.entry.api.EntryCustomType;
import com.tsu.enums.BaseCustomType;
import com.tsu.enums.BaseExceptionCode;
import com.tsu.enums.BaseParamName;
import com.tsu.enums.NamespaceNodeType;
import com.tsu.namespace.api.Entity;
import com.tsu.namespace.api.EntityManager;
import com.tsu.namespace.api.EntityType;
import com.tsu.namespace.api.Namespace;
import com.tsu.auth.permissions.NamespaceAction;
import com.tsu.namespace.api.namespace.NamespaceObjectFactory;
import com.tsu.namespace.helper.EntityDbHelper;
import com.tsu.namespace.record.EntityRecord;
import com.tsu.namespace.record.EntityTypeRecord;
import com.tsu.workspace.request.AddEntity;
import com.tsu.workspace.val.EntityTypeVal;
import com.tsu.common.api.ActionPack;
import com.tsu.common.exception.UserException;
import com.tsu.common.utils.LazyCacheLoader;
import com.tsu.common.utils.ParamValidator;
import com.tsu.common.vo.Text;
import com.tsu.entry.api.Node;
import com.tsu.auth.security.NamespaceContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public class NamespaceEntityManager implements EntityManager {

    private final Namespace namespace;
    private final NamespaceContext context;
    private final EntityDbHelper entityDbHelper;
    private final NamespaceObjectFactory factory;
    private final LazyCacheLoader<List<EntityTypeRecord>> entityTypes;

    public NamespaceEntityManager(NamespaceContext context,
                                  EntityDbHelper entityDbHelper, NamespaceObjectFactory factory) {
        this.namespace = context.getNamespace();
        this.context = context;
        this.entityDbHelper = entityDbHelper;
        this.factory = factory;
        this.entityTypes = LazyCacheLoader.of(() -> entityDbHelper.findEntityTypesByNamespaceId(namespace.getId())
                .toList()
        );
    }

    @Override
    public Optional<Entity> findEntity(UUID entityId) {
        return entityDbHelper.findEntityByNamespaceIdAndId(namespace.getId(), entityId, context)
                .map(e -> factory.build(namespace, e, context));
    }

    @Override
    public Optional<EntityType> findEntityType(String name) {
        return entityDbHelper.findEntityTypeByNamespaceIdAndType(namespace.getId(), name)
                .map(r -> factory.build(namespace, r));
    }

    @Override
    public Entity addEntity(AddEntity request, Object profile) {
        ParamValidator.builder()
                .withNonNullOrEmpty(request.getType(), BaseParamName.TYPE)
                .withVerifyEmail(request.getEmail())
                .withNonNullOrEmpty(request.getName(), BaseParamName.NAME)
                .throwIfErrors();
        EntityTypeRecord type = entityDbHelper.findEntityTypeByNamespaceIdAndType(namespace.getId(), request.getType().strip())
                .orElseThrow(() -> new UserException(BaseExceptionCode.ENTITY_TYPE_NOT_EXISTS, Map.of(BaseParamName.TYPE, request.getType())));
        if (entityDbHelper.findEntityByNamespaceIdAndNameAndTypeId(namespace.getId(), request.getName().strip(), type.getId(), context).isPresent()) {
            throw new UserException(BaseExceptionCode.ENTITY_EXISTS, Map.of(BaseParamName.NAME, request.getName(), BaseParamName.TYPE, request.getType()));
        }
        Node node = namespace.getNode().addNode(NamespaceNodeType.ENTITY);
        UUID parentId = Optional.ofNullable(request.getParentId())
                .flatMap(this::findEntity)
                .map(Entity::getId)
                .orElse(null);
        Integer placeId = Optional.ofNullable(request.getPlaceId())
                .filter(id -> namespace.getPlaceManager().findPlaceById(id).isPresent())
                .orElse(null);
        EntityRecord e = entityDbHelper.addEntity(node.getId(), namespace.getId(), type, request.getName().strip(), request.getEmail().toString(), request.getPhone(), profile,
                parentId, placeId, context);
        return factory.build(namespace, e, context);
    }

    @Override
    public EntityTypeVal addEntityType(Text name) {
        ParamValidator.builder()
                .withNonNullOrEmpty(name, BaseParamName.NAME)
                .throwIfErrors();
        if (entityDbHelper.findEntityTypeByNamespaceIdAndType(namespace.getId(), name.strip()).isPresent()) {
            throw new UserException(BaseExceptionCode.ENTITY_TYPE_EXISTS, Map.of(BaseParamName.NAME, name.strip()));
        }
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.CREATE_ENTITY_TYPE, name));
        EntityTypeRecord entityTypeRecord = entityDbHelper.addEntityType(namespace.getId(), name.strip(), context);
        return entityTypeRecord.getValue();
    }


    @Override
    public Stream<EntityTypeVal> getEntityTypes() {
        return entityTypes.get().stream()
                .map(EntityTypeRecord::getValue);
    }

}
