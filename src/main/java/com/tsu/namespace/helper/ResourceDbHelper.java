package com.tsu.namespace.helper;

import com.tsu.auth.security.AppSecurityContext;
import com.tsu.common.jpa.JsonValueUtils;
import com.tsu.common.jpa.Jsonb;
import com.tsu.namespace.api.ResourceType;
import com.tsu.namespace.entities.ResourceExceptionTb;
import com.tsu.namespace.entities.ResourceTb;
import com.tsu.namespace.entities.ResourceWeeklyWindowTb;
import com.tsu.namespace.entities.id.ResourceExceptionId;
import com.tsu.namespace.entities.id.ResourceId;
import com.tsu.namespace.entities.id.ResourceWeeklyWindowId;
import com.tsu.namespace.record.ResourceExceptionRecord;
import com.tsu.namespace.record.ResourceRecord;
import com.tsu.namespace.record.ResourceWeeklyWindowRecord;
import com.tsu.namespace.record.TimeSlotRecord;
import com.tsu.namespace.repo.ResourceExceptionRepository;
import com.tsu.namespace.repo.ResourceRepository;
import com.tsu.namespace.repo.ResourceWeeklyWindowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Database helper for resource operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceDbHelper {

    private final ResourceRepository resourceRepository;
    private final ResourceWeeklyWindowRepository weeklyWindowRepository;
    private final ResourceExceptionRepository exceptionRepository;

    public ResourceRecord createResource(UUID namespaceId,UUID resourceId, ResourceType type, String name,
                                        Integer capacity, String location, String timezone,
                                        Object meta, boolean active, AppSecurityContext context) {
        log.debug("Creating resource: namespaceId={}, type={}, name={}", namespaceId, type, name);

        ResourceTb tb = new ResourceTb();
        ResourceId id = new ResourceId(namespaceId, resourceId);
        tb.setId(id);
        tb.setType(type);
        tb.setName(name);
        tb.setCapacity(capacity);
        tb.setLocation(location);
        tb.setTimezone(timezone);
        tb.setMeta(JsonValueUtils.getInstance().encodeAsJson(meta));
        tb.setActive(active);
        tb.setCreatedDate(LocalDateTime.now());
        tb.setCreatedBy(context.getPrincipal().id());
        tb.setModifiedDate(LocalDateTime.now());
        tb.setModifiedBy(context.getPrincipal().id());
        resourceRepository.save(tb);
        log.info("Resource created with id: {}", id.getId());

        return build(tb, context);
    }

    public Optional<ResourceRecord> findResourceById(UUID namespaceId, UUID resourceId, AppSecurityContext context) {
        ResourceId id = new ResourceId(namespaceId, resourceId);
        return resourceRepository.findById(id)
                .map(tb -> build(tb, context));
    }

    public Page<ResourceRecord> findAllResources(UUID namespaceId, Pageable pageable, AppSecurityContext context) {
        return resourceRepository.findByIdNamespaceId(namespaceId, pageable)
                .map(tb -> build(tb, context));
    }

    public Page<ResourceRecord> findActiveResources(UUID namespaceId, Pageable pageable, AppSecurityContext context) {
        return resourceRepository.findByIdNamespaceIdAndActiveTrue(namespaceId, pageable)
                .map(tb -> build(tb, context));
    }

    public Page<ResourceRecord> findResourcesByType(UUID namespaceId, ResourceType type,
                                                   Pageable pageable, AppSecurityContext context) {
        return resourceRepository.findByIdNamespaceIdAndType(namespaceId, type, pageable)
                .map(tb -> build(tb, context));
    }

    public Page<ResourceRecord> findActiveResourcesByType(UUID namespaceId, ResourceType type,
                                                         Pageable pageable, AppSecurityContext context) {
        return resourceRepository.findByIdNamespaceIdAndTypeAndActiveTrue(namespaceId, type, pageable)
                .map(tb -> build(tb, context));
    }

    public Page<ResourceRecord> findResourcesByLocation(UUID namespaceId, String location,
                                                       Pageable pageable, AppSecurityContext context) {
        return resourceRepository.findByNamespaceIdAndLocation(namespaceId, location, pageable)
                .map(tb -> build(tb, context));
    }

    public ResourceRecord updateResource(UUID namespaceId, UUID resourceId, String name,
                                        Integer capacity, String location, String timezone,
                                        Object meta, AppSecurityContext context) {
        ResourceId id = new ResourceId(namespaceId, resourceId);
        ResourceTb tb = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));

        if (name != null) tb.setName(name);
        if (capacity != null) tb.setCapacity(capacity);
        if (location != null) tb.setLocation(location);
        if (timezone != null) tb.setTimezone(timezone);
        if (meta != null) tb.setMeta(meta);

        tb.setModifiedDate(LocalDateTime.now());
        tb.setUpdatedBy(context.getUserId());

        resourceRepository.save(tb);
        log.info("Resource updated: {}", resourceId);

        return build(tb, context);
    }

    public ResourceRecord setResourceActive(UUID namespaceId, UUID resourceId, boolean active,
                                           AppSecurityContext context) {
        ResourceId id = new ResourceId(namespaceId, resourceId);
        ResourceTb tb = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));

        tb.setActive(active);
        tb.setUpdatedAt(LocalDateTime.now());
        tb.setUpdatedBy(context.getUserId());

        resourceRepository.save(tb);
        log.info("Resource {} {}", resourceId, active ? "activated" : "deactivated");

        return build(tb, context);
    }

    public void deleteResource(UUID namespaceId, UUID resourceId) {
        ResourceId id = new ResourceId(namespaceId, resourceId);
        resourceRepository.deleteById(id);
        log.info("Resource deleted: {}", resourceId);
    }

    // Weekly Windows

    public ResourceWeeklyWindowRecord addWeeklyWindow(UUID namespaceId, UUID resourceId,
                                                     Integer dayOfWeek, LocalTime startLocal,
                                                     LocalTime endLocal, AppSecurityContext context) {
        log.debug("Adding weekly window: resourceId={}, dow={}, start={}, end={}",
                resourceId, dayOfWeek, startLocal, endLocal);

        ResourceWeeklyWindowTb tb = new ResourceWeeklyWindowTb();
        tb.setNamespaceId(namespaceId);
        tb.setResourceId(resourceId);
        tb.setDayOfWeek(dayOfWeek);
        tb.setStartLocal(startLocal);
        tb.setEndLocal(endLocal);

        weeklyWindowRepository.save(tb);
        log.info("Weekly window created with id: {}", tb.getId());

        return buildWeeklyWindow(tb, context);
    }

    public List<ResourceWeeklyWindowRecord> findWeeklyWindows(UUID namespaceId, UUID resourceId,
                                                             AppSecurityContext context) {
        return weeklyWindowRepository.findByNamespaceIdAndResourceIdOrderByDayOfWeekAndStartLocal(
                namespaceId, resourceId)
                .stream()
                .map(tb -> buildWeeklyWindow(tb, context))
                .toList();
    }

    public void deleteWeeklyWindow(UUID namespaceId, Long windowId) {
        ResourceWeeklyWindowId id = new ResourceWeeklyWindowId(namespaceId, windowId);
        weeklyWindowRepository.deleteById(id);
        log.info("Weekly window deleted: {}", windowId);
    }

    // Exceptions

    public ResourceExceptionRecord addException(UUID namespaceId, UUID resourceId,
                                               LocalDateTime startAt, LocalDateTime endAt,
                                               String reason, AppSecurityContext context) {
        log.debug("Adding resource exception: resourceId={}, start={}, end={}",
                resourceId, startAt, endAt);

        ResourceExceptionTb tb = new ResourceExceptionTb();
        tb.setNamespaceId(namespaceId);
        tb.setResourceId(resourceId);
        tb.setStartAt(startAt);
        tb.setEndAt(endAt);
        tb.setReason(reason);

        exceptionRepository.save(tb);
        log.info("Resource exception created with id: {}", tb.getId());

        return buildException(tb, context);
    }

    public List<ResourceExceptionRecord> findExceptions(UUID namespaceId, UUID resourceId,
                                                       AppSecurityContext context) {
        return exceptionRepository.findByNamespaceIdAndResourceIdOrderByStartAt(namespaceId, resourceId)
                .stream()
                .map(tb -> buildException(tb, context))
                .toList();
    }

    public List<ResourceExceptionRecord> findActiveExceptions(UUID namespaceId, UUID resourceId,
                                                             AppSecurityContext context) {
        LocalDateTime now = LocalDateTime.now();
        return exceptionRepository.findActiveExceptions(namespaceId, resourceId, now)
                .stream()
                .map(tb -> buildException(tb, context))
                .toList();
    }

    public void deleteException(UUID namespaceId, Long exceptionId) {
        ResourceExceptionId id = new ResourceExceptionId(namespaceId, exceptionId);
        exceptionRepository.deleteById(id);
        log.info("Resource exception deleted: {}", exceptionId);
    }

    // Availability checking (simplified)
    public boolean isAvailable(UUID namespaceId, UUID resourceId,
                              LocalDateTime startAt, LocalDateTime endAt) {
        // Check if there are any exceptions overlapping
        List<ResourceExceptionTb> exceptions = exceptionRepository.findOverlappingExceptions(
                namespaceId, resourceId, startAt, endAt);

        return exceptions.isEmpty();
    }

    public List<TimeSlotRecord> findAvailableSlots(UUID namespaceId, UUID resourceId,
                                                   LocalDateTime startDate, LocalDateTime endDate,
                                                   Integer durationMinutes, AppSecurityContext context) {
        // This is a simplified implementation - full implementation would need to
        // check weekly windows and existing bookings
        log.warn("findAvailableSlots is not fully implemented yet");
        return List.of();
    }

    // Record builders

    private ResourceRecord build(ResourceTb tb, AppSecurityContext context) {
        return new ResourceRecord(tb, context);
    }

    private ResourceWeeklyWindowRecord buildWeeklyWindow(ResourceWeeklyWindowTb tb, AppSecurityContext context) {
        return new ResourceWeeklyWindowRecord(tb, context);
    }

    private ResourceExceptionRecord buildException(ResourceExceptionTb tb, AppSecurityContext context) {
        return new ResourceExceptionRecord(tb, context);
    }
}
