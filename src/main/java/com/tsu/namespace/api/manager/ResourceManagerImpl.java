package com.tsu.namespace.api.manager;

import com.tsu.auth.permissions.NamespaceAction;
import com.tsu.auth.permissions.ResourcePermission;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.common.api.ActionPack;
import com.tsu.entry.api.Node;
import com.tsu.enums.NamespaceNodeType;
import com.tsu.namespace.api.*;
import com.tsu.namespace.helper.ResourceDbHelper;
import com.tsu.namespace.record.ResourceExceptionRecord;
import com.tsu.namespace.record.ResourceRecord;
import com.tsu.namespace.record.ResourceWeeklyWindowRecord;
import com.tsu.namespace.service.IDGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of ResourceManager for namespace-scoped resource management.
 */
@Slf4j
@RequiredArgsConstructor
public class ResourceManagerImpl implements ResourceManager {

    private final Namespace namespace;
    private final AppSecurityContext context;
    private final ResourceDbHelper dbHelper;
    private final IDGeneratorService idGeneratorService;

    @Override
    public Resource createResource(ResourceType type, String name, Integer capacity,
                                   String location, String timezone, String meta) {
        log.debug("Creating resource: type={}, name={}", type, name);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(NamespaceAction.CREATE_RESOURCES, Map.of(
                        "type", type,
                        "name", name
                ))
        );
        Node resource = namespace.getNode().addNode(NamespaceNodeType.RESOURCE);
        ResourceRecord record = dbHelper.createResource(
                namespace.getId(), resource.getId(), type, name, capacity, location, timezone, meta, true, context
        );
        return record;
    }

    @Override
    public Optional<Resource> getResource(UUID resourceId) {
        return dbHelper.findResourceById(namespace.getId(), resourceId, context)
                .map(r -> (Resource) r);
    }

    @Override
    public Page<Resource> getResources(Pageable pageable) {
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(NamespaceAction.VIEW_RESOURCES, Map.of())
        );

        return dbHelper.findAllResources(namespace.getId(), pageable, context)
                .map(r -> (Resource) r);
    }

    @Override
    public Page<Resource> getActiveResources(Pageable pageable) {
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(NamespaceAction.VIEW_RESOURCES, Map.of())
        );
        return dbHelper.findActiveResources(namespace.getId(), pageable, context)
                .map(r -> (Resource) r);
    }

    @Override
    public Page<Resource> getResourcesByType(ResourceType type, Pageable pageable) {
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(NamespaceAction.VIEW_RESOURCES, Map.of("type", type))
        );

        return dbHelper.findResourcesByType(namespace.getId(), type, pageable, context)
                .map(r -> (Resource) r);
    }

    @Override
    public Page<Resource> getActiveResourcesByType(ResourceType type, Pageable pageable) {
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(NamespaceAction.VIEW_RESOURCES, Map.of("type", type))
        );

        return dbHelper.findActiveResourcesByType(namespace.getId(), type, pageable, context)
                .map(r -> (Resource) r);
    }

    @Override
    public Page<Resource> getResourcesByLocation(String location, Pageable pageable) {
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(NamespaceAction.VIEW_RESOURCES, Map.of("location", location))
        );

        return dbHelper.findResourcesByLocation(namespace.getId(), location, pageable, context)
                .map(r -> (Resource) r);
    }

    @Override
    public Resource updateResource(UUID resourceId, String name, Integer capacity,
                                   String location, String timezone, String meta) {
        log.debug("Updating resource: {}", resourceId);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(NamespaceAction.UPDATE_RESOURCES,Map.of("resourceId", resourceId))
        );

        ResourceRecord record = dbHelper.updateResource(
                namespace.getId(), resourceId, name, capacity, location, timezone, meta, context
        );

        return record;
    }

    @Override
    public Resource activateResource(UUID resourceId) {
        log.debug("Activating resource: {}", resourceId);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(ResourcePermission.ACTIVATE, Map.of("resourceId", resourceId))
        );

        return dbHelper.setResourceActive(namespace.getId(), resourceId, true, context);
    }

    @Override
    public Resource deactivateResource(UUID resourceId) {
        log.debug("Deactivating resource: {}", resourceId);
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(NamespaceAction.MANAGE_RESOURCES, Map.of("resourceId", resourceId))
        );
        return dbHelper.setResourceActive(namespace.getId(), resourceId, false, context);
    }

    @Override
    public void deleteResource(UUID resourceId) {
        log.debug("Deleting resource: {}", resourceId);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(NamespaceAction.MANAGE_RESOURCES, Map.of("resourceId", resourceId))
        );

        dbHelper.deleteResource(namespace.getId(), resourceId);
    }

    @Override
    public ResourceWeeklyWindow addWeeklyWindow(UUID resourceId, Integer dayOfWeek,
                                                String startLocal, String endLocal) {
        log.debug("Adding weekly window: resourceId={}, dow={}", resourceId, dayOfWeek);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(NamespaceAction.MANAGE_RESOURCES, Map.of("resourceId", resourceId))
        );

        LocalTime start = LocalTime.parse(startLocal);
        LocalTime end = LocalTime.parse(endLocal);

        ResourceWeeklyWindowRecord record = dbHelper.addWeeklyWindow(
                namespace.getId(), resourceId, dayOfWeek, start, end, context
        );

        return record;
    }

    @Override
    public List<ResourceWeeklyWindow> getWeeklyWindows(UUID resourceId) {
        return dbHelper.findWeeklyWindows(namespace.getId(), resourceId, context)
                .stream()
                .map(r -> (ResourceWeeklyWindow) r)
                .toList();
    }

    @Override
    public void removeWeeklyWindow(Long windowId) {
        log.debug("Removing weekly window: {}", windowId);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(NamespaceAction.MANAGE_RESOURCES, Map.of("windowId", windowId))
        );

        dbHelper.deleteWeeklyWindow(namespace.getId(), windowId);
    }

    @Override
    public ResourceException addException(UUID resourceId, LocalDateTime startAt,
                                          LocalDateTime endAt, String reason) {
        log.debug("Adding resource exception: resourceId={}, start={}, end={}", resourceId, startAt, endAt);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(NamespaceAction.MANAGE_RESOURCES, Map.of("resourceId", resourceId))
        );

        ResourceExceptionRecord record = dbHelper.addException(
                namespace.getId(), resourceId, startAt, endAt, reason, context
        );

        return record;
    }

    @Override
    public List<ResourceException> getExceptions(UUID resourceId) {
        return dbHelper.findExceptions(namespace.getId(), resourceId, context)
                .stream()
                .map(r -> (ResourceException) r)
                .toList();
    }

    @Override
    public List<ResourceException> getActiveExceptions(UUID resourceId) {
        return dbHelper.findActiveExceptions(namespace.getId(), resourceId, context)
                .stream()
                .map(r -> (ResourceException) r)
                .toList();
    }

    @Override
    public void removeException(Long exceptionId) {
        log.debug("Removing resource exception: {}", exceptionId);

        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(NamespaceAction.MANAGE_RESOURCES, Map.of("exceptionId", exceptionId))
        );

        dbHelper.deleteException(namespace.getId(), exceptionId);
    }

    @Override
    public boolean isAvailable(UUID resourceId, LocalDateTime startAt, LocalDateTime endAt) {
        return dbHelper.isAvailable(namespace.getId(), resourceId, startAt, endAt);
    }

    @Override
    public List<TimeSlot> findAvailableSlots(UUID resourceId, LocalDateTime startDate,
                                             LocalDateTime endDate, Integer durationMinutes) {
        namespace.getPermissionManager().auditAndCheckPermission(
                new ActionPack(NamespaceAction.VIEW_RESOURCES, Map.of("resourceId", resourceId))
        );

        return dbHelper.findAvailableSlots(namespace.getId(), resourceId, startDate, endDate, durationMinutes, context)
                .stream()
                .map(r -> (TimeSlot) r)
                .toList();
    }
}
