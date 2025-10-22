package com.tsu.namespace.helper;

import com.tsu.common.data.PermissionData;
import com.tsu.namespace.api.NamespaceUserType;
import com.tsu.namespace.entities.NamespaceRoleTb;
import com.tsu.namespace.entities.NamespaceTb;
import com.tsu.namespace.entities.NamespaceUserTb;
import com.tsu.namespace.entities.NamespaceUserViewTb;
import com.tsu.namespace.entities.id.NamespaceRoleId;
import com.tsu.namespace.entities.id.NamespaceUserId;
import com.tsu.auth.api.AccessLevel;
import com.tsu.namespace.record.NamespaceRecord;
import com.tsu.namespace.record.NamespaceRoleRecord;
import com.tsu.namespace.record.NamespaceUserRecord;
import com.tsu.namespace.repo.*;
import com.tsu.workspace.request.UserFilter;
import com.tsu.namespace.val.NspUsrVal;
import com.tsu.auth.api.BasePrincipal;
import com.tsu.common.jpa.JsonValueUtils;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.auth.security.NamespaceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class NamespaceDbHelper {

    private final NamespaceRepository namespaceRepository;
    private final NamespaceUserRepository namespaceUserRepository;
    private final NamespaceRoleRepository namespaceRoleRepository;
    private final NamespaceUserViewRepository namespaceUserViewRepository;

    public Stream<NamespaceRecord> getAll(AppSecurityContext context) {
        log.debug("Retrieving all namespaces for user: {}", context.getPrincipal().id());
        Stream<NamespaceRecord> namespaces = namespaceRepository.findAll().stream()
                .map(v -> toVal(v, context));
        log.trace("Namespace stream created for all namespaces");
        return namespaces;
    }

    public Optional<NamespaceRecord> findById(UUID id, AppSecurityContext context) {
        log.debug("Looking up namespace by ID: {}", id);
        Optional<NamespaceRecord> namespace = namespaceRepository.findById(id)
                .map(v -> {
                    log.trace("Namespace found with ID: {} and name: {}", id, v.getName());
                    return toVal(v, context);
                });
        if (namespace.isEmpty()) {
            log.debug("No namespace found with ID: {}", id);
        }
        return namespace;
    }

    public Optional<NamespaceRecord> findByName(String namespace, AppSecurityContext context) {
        log.debug("Looking up namespace by name: {}", namespace);
        Optional<NamespaceRecord> result = namespaceRepository.findByName(namespace)
                .map(v -> {
                    log.trace("Namespace found with name: {} and ID: {}", namespace, v.getId());
                    return toVal(v, context);
                });
        if (result.isEmpty()) {
            log.debug("No namespace found with name: {}", namespace);
        }
        return result;
    }

    public Optional<UUID> checkUriAvailabilityForNamespace(String uri) {
        return namespaceRepository.findByUri(uri).map(NamespaceTb::getId);
    }

    public Optional<UUID> checkNameAvailabilityForNamespace(String name) {
        return namespaceRepository.findByName(name).map(NamespaceTb::getId);
    }

    public Optional<NamespaceRecord> findByUri(String uri, AppSecurityContext context) {
        log.debug("Looking up namespace by URI: {}", uri);
        Optional<NamespaceRecord> result = namespaceRepository.findByUri(uri)
                .map(v -> {
                    log.trace("Namespace found with URI: {} and ID: {}", uri, v.getId());
                    return toVal(v, context);
                });
        if (result.isEmpty()) {
            log.debug("No namespace found with URI: {}", uri);
        }
        return result;
    }

    private NamespaceRoleRecord toVal(NamespaceRoleTb e, NamespaceContext context) {
        log.trace("Converting NamespaceRoleTb to NamespaceRoleRecord for namespace: {}", e.getName());
        return new NamespaceRoleRecord(e, tb -> {
            log.trace("Persisting namespace role changes for ID: {} by user: {}", tb.getId(), context.getNamespaceUserId());
            tb.setModifiedBy(context.getNamespaceUserId());
            tb.setModifiedDate(LocalDateTime.now());
            namespaceRoleRepository.save(tb);
            log.debug("Namespace role persisted successfully: {}", tb.getId());
        });
    }


    private NamespaceRecord toVal(NamespaceTb e, AppSecurityContext context) {
        log.trace("Converting NamespaceTb to NamespaceRecord for namespace: {}", e.getName());
        return new NamespaceRecord(e, tb -> {
            log.trace("Persisting namespace changes for ID: {} by user: {}", tb.getId(), context.getPrincipal().id());
            tb.setModifiedBy(context.getPrincipal().id());
            tb.setModifiedDate(LocalDateTime.now());
            namespaceRepository.save(tb);
            log.debug("Namespace persisted successfully: {}", tb.getId());
        });
    }

    public NamespaceRecord createNamespace(BasePrincipal namespaceId, String name, String uri, String website, String contactEmail, String bucket,
                                           String description, String logImageUrl, String backgroundImageUrl, AccessLevel accessLevel, Object props, AppSecurityContext context) {
        log.info("Creating new namespace({}): {} with URI: {} in bucket: {}", namespaceId, name, uri, bucket);
        NamespaceTb tb = new NamespaceTb();
        tb.setId(namespaceId.id());
        tb.setName(name);
        tb.setUri(uri);
        tb.setBucket(bucket);
        tb.setContactEmail(contactEmail);
        tb.setLogoImageUrl(logImageUrl);
        tb.setOwnerId(context.getPrincipal().id());
        tb.setDescription(description);
        tb.setBackgroundImageUrl(backgroundImageUrl);
        tb.setAccessLevel(accessLevel);
        tb.setWebsite(website);
        tb.setActive(true);
        tb.setProps(JsonValueUtils.getInstance().encodeAsJson(props));
        tb.setCreatedBy(context.getPrincipal().id());
        tb.setCreatedDate(LocalDateTime.now());
        tb.setModifiedBy(context.getPrincipal().id());
        tb.setModifiedDate(LocalDateTime.now());
        log.debug("Saving namespace entity to database: {}", namespaceId);
        namespaceRepository.save(tb);
        log.info("Namespace created successfully - ID: {}, name: {}, owner: {}",
                namespaceId, name, context.getPrincipal().id());
        return toVal(tb, context);
    }

    public Stream<NamespaceRecord> findByOwnerId(UUID ownerId, AppSecurityContext context) {
        return namespaceRepository.findByOwnerId(ownerId)
                .map(tb -> toVal(tb, context));
    }

    public Stream<NamespaceRecord> findByPrincipalId(UUID userId, AppSecurityContext context) {
        return namespaceRepository.findByPrincipalId(userId)
                .map(tb -> toVal(tb, context));
    }

    public Optional<NamespaceRecord> findByNamespaceIdAndUserId(UUID namespaceId, UUID userId, AppSecurityContext context) {
        return namespaceRepository.findByNamespaceIdAndPrincipalId(namespaceId, userId)
                .map(tb -> toVal(tb, context));
    }

    public Optional<NamespaceUserRecord> findNamespaceUserByNamespaceIdAndId(UUID namespaceId, Integer userId, NamespaceContext context) {
        return namespaceUserRepository.findById(new NamespaceUserId(namespaceId, userId))
                .map(tb -> toVal(tb, context));
    }

    public NamespaceRoleRecord addNamespaceRole(String name, String description, List<PermissionData> permissions, NamespaceContext context) {
        NamespaceRoleTb tb = new NamespaceRoleTb();
        tb.setId(new NamespaceRoleId(context.getNamespace().getId(), null));
        tb.setPermissions(JsonValueUtils.getInstance().encodeAsJson(permissions));
        tb.setName(name);
        tb.setDescription(description);
        tb.setCreatedBy(context.getNamespaceUserId());
        tb.setCreatedDate(LocalDateTime.now());
        tb.setModifiedBy(context.getNamespaceUserId());
        tb.setModifiedDate(LocalDateTime.now());
        namespaceRoleRepository.save(tb);
        return toVal(tb, context);
    }

    public NamespaceUserRecord addNamespaceUser(UUID namespaceId, BasePrincipal user, String displayName, NamespaceUserType type, boolean active,
                                                LocalDateTime approvedDate, BasePrincipal approvedBy,
                                                LocalDate expirationDate, AppSecurityContext context) {
        BasePrincipal principal = context.getPrincipal();
        NamespaceUserTb tb = new NamespaceUserTb();
        tb.setId(new NamespaceUserId(namespaceId, null));
        tb.setPrincipalId(user.id());
        tb.setActive(active);
        tb.setDisplayName(displayName);
        tb.setCreatedBy(principal.id());
        tb.setCreatedDate(LocalDateTime.now());
        tb.setModifiedBy(principal.id());
        tb.setModifiedDate(LocalDateTime.now());
        tb.setType(type);
        tb.setExpirationDate(expirationDate);
        if (approvedDate != null) {
            tb.setApprovedBy(Optional.ofNullable(approvedBy).map(BasePrincipal::id).orElse(principal.id()));
            tb.setApprovedDate(approvedDate);
        }
        if (active) {
            tb.setActivationDate(LocalDate.now());
        }
        tb.setPermissions(JsonValueUtils.getInstance().encodeAsJson(List.of()));
        namespaceUserRepository.save(tb);
        return toVal(tb, context);
    }

    public Stream<NamespaceUserRecord> findNamespaceUserByNamespaceIdAndType(UUID namespaceId, NamespaceUserType type, NamespaceContext context) {
        return namespaceUserRepository.findByIdNamespaceIdAndType(namespaceId, type)
                .map(tb -> toVal(tb, context));
    }

    public Stream<NamespaceUserRecord> findNamespaceUserByNamespaceId(UUID namespaceId, NamespaceContext context) {
        return namespaceUserRepository.findByIdNamespaceId(namespaceId)
                .map(tb -> toVal(tb, context));
    }

    public Optional<NamespaceUserRecord> findNamespaceUserByNamespaceIdAndPrincipalId(UUID namespaceId, UUID principalId, AppSecurityContext context) {
        return namespaceUserRepository.findByIdNamespaceIdAndPrincipalId(namespaceId, principalId)
                .map(tb -> toVal(tb, context));
    }

    private NamespaceUserRecord toVal(NamespaceUserTb e, NamespaceContext context) {
        return new NamespaceUserRecord(e, tb -> {
            tb.setModifiedBy(context.getSecurityContext().getPrincipal().id());
            tb.setModifiedDate(LocalDateTime.now());
            namespaceUserRepository.save(tb);
        }, role -> {
            role.setModifiedBy(context.getNamespaceUserId());
            role.setModifiedDate(LocalDateTime.now());
            namespaceRoleRepository.save(role);
        });
    }

    private NamespaceUserRecord toVal(NamespaceUserTb e, AppSecurityContext context) {
        return new NamespaceUserRecord(e, tb -> {
            tb.setModifiedBy(context.getPrincipal().id());
            tb.setModifiedDate(LocalDateTime.now());
            namespaceUserRepository.save(tb);
        }, role -> {
            role.setModifiedBy(e.getId().getId());
            role.setModifiedDate(LocalDateTime.now());
            namespaceRoleRepository.save(role);
        });
    }

    public Stream<NamespaceRoleRecord> findNamespaceRoles(UUID id, NamespaceContext context) {
        return namespaceRoleRepository.findByIdNamespaceIdOrderByName(id)
                .map(tb -> toVal(tb, context));
    }

    public Stream<NspUsrVal> findNamespaceJoinedUserInfoByNamespaceId(UUID namespaceId) {
        return namespaceUserRepository.findNamespaceJoinedUserInfoByNamespaceId(namespaceId);
    }

    public Page<NamespaceUserViewTb> queryUsers(UUID namespaceId, UserFilter filter, Pageable pageable, NamespaceContext context) {
        // Set namespace ID in filter and build specification
        filter.setNamespaceId(namespaceId);
        Specification<NamespaceUserViewTb> spec = NamespaceUserSpecifications.fromFilter(filter);

        return namespaceUserViewRepository.findAll(spec, pageable);
    }


}
