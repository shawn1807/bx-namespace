package com.tsu.namespace.repo;

import com.tsu.base.api.NamespaceUserType;
import com.tsu.namespace.api.SecurityClass;
import com.tsu.namespace.entities.NamespaceUserViewTb;
import com.tsu.base.request.UserFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Reusable JPA Specifications for NamespaceUserViewTb (materialized view) queries.
 * Each method returns a composable Specification that can be combined with others.
 *
 * These specifications operate on the denormalized namespace_user_mv materialized view
 * which includes joined data from user_base and namespace.
 */
public class NamespaceUserSpecifications {

    /**
     * Filter by namespace ID (required for most queries)
     */
    public static Specification<NamespaceUserViewTb> hasNamespaceId(UUID namespaceId) {
        return (root, query, cb) -> {
            if (namespaceId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("namespaceId"), namespaceId);
        };
    }

    /**
     * Filter by principal ID
     */
    public static Specification<NamespaceUserViewTb> hasPrincipalId(UUID principalId) {
        return (root, query, cb) -> {
            if (principalId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("principalId"), principalId);
        };
    }

    /**
     * Filter by display name (partial match, case-insensitive)
     */
    public static Specification<NamespaceUserViewTb> displayNameContains(String displayName) {
        return (root, query, cb) -> {
            if (displayName == null || displayName.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + displayName.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("displayName")), pattern);
        };
    }

    /**
     * Filter by email (partial match, case-insensitive)
     */
    public static Specification<NamespaceUserViewTb> emailContains(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + email.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("email")), pattern);
        };
    }

    /**
     * Filter by first name (partial match, case-insensitive)
     */
    public static Specification<NamespaceUserViewTb> firstNameContains(String firstName) {
        return (root, query, cb) -> {
            if (firstName == null || firstName.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + firstName.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("firstName")), pattern);
        };
    }

    /**
     * Filter by last name (partial match, case-insensitive)
     */
    public static Specification<NamespaceUserViewTb> lastNameContains(String lastName) {
        return (root, query, cb) -> {
            if (lastName == null || lastName.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + lastName.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("lastName")), pattern);
        };
    }

    /**
     * Filter by user type
     */
    public static Specification<NamespaceUserViewTb> hasType(NamespaceUserType type) {
        return (root, query, cb) -> {
            if (type == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("type"), type);
        };
    }

    /**
     * Filter by security level
     */
    public static Specification<NamespaceUserViewTb> hasSecurityLevel(SecurityClass securityLevel) {
        return (root, query, cb) -> {
            if (securityLevel == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("securityLevel"), securityLevel);
        };
    }

    /**
     * Filter by active status
     */
    public static Specification<NamespaceUserViewTb> isActive(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("active"), active);
        };
    }

    /**
     * Filter by role ID
     */
    public static Specification<NamespaceUserViewTb> hasRoleId(Integer roleId) {
        return (root, query, cb) -> {
            if (roleId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("roleId"), roleId);
        };
    }

    /**
     * Filter by activation date from (inclusive)
     */
    public static Specification<NamespaceUserViewTb> activationDateFrom(LocalDate activationDateFrom) {
        return (root, query, cb) -> {
            if (activationDateFrom == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("activationDate"), activationDateFrom);
        };
    }

    /**
     * Filter by activation date to (inclusive)
     */
    public static Specification<NamespaceUserViewTb> activationDateTo(LocalDate activationDateTo) {
        return (root, query, cb) -> {
            if (activationDateTo == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("activationDate"), activationDateTo);
        };
    }

    /**
     * Filter by expiration date from (inclusive)
     */
    public static Specification<NamespaceUserViewTb> expirationDateFrom(LocalDate expirationDateFrom) {
        return (root, query, cb) -> {
            if (expirationDateFrom == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("expirationDate"), expirationDateFrom);
        };
    }

    /**
     * Filter by expiration date to (inclusive)
     */
    public static Specification<NamespaceUserViewTb> expirationDateTo(LocalDate expirationDateTo) {
        return (root, query, cb) -> {
            if (expirationDateTo == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("expirationDate"), expirationDateTo);
        };
    }

    /**
     * Full-text search across display_name, email, first_name, and last_name
     * Returns results matching any of these fields
     */
    public static Specification<NamespaceUserViewTb> searchTerm(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("displayName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern)
            );
        };
    }

    /**
     * Build a composite specification from a UserFilter object
     * Combines all non-null filter criteria with AND logic
     */
    public static Specification<NamespaceUserViewTb> fromFilter(UserFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            // Apply all filter criteria
            if (filter.getNamespaceId() != null) {
                predicates.add(hasNamespaceId(filter.getNamespaceId()).toPredicate(root, query, cb));
            }
            if (filter.getPrincipalId() != null) {
                predicates.add(hasPrincipalId(filter.getPrincipalId()).toPredicate(root, query, cb));
            }
            if (filter.getDisplayName() != null && !filter.getDisplayName().isEmpty()) {
                predicates.add(displayNameContains(filter.getDisplayName()).toPredicate(root, query, cb));
            }
            if (filter.getEmail() != null && !filter.getEmail().isEmpty()) {
                predicates.add(emailContains(filter.getEmail()).toPredicate(root, query, cb));
            }
            if (filter.getFirstName() != null && !filter.getFirstName().isEmpty()) {
                predicates.add(firstNameContains(filter.getFirstName()).toPredicate(root, query, cb));
            }
            if (filter.getLastName() != null && !filter.getLastName().isEmpty()) {
                predicates.add(lastNameContains(filter.getLastName()).toPredicate(root, query, cb));
            }
            if (filter.getType() != null) {
                predicates.add(hasType(filter.getType()).toPredicate(root, query, cb));
            }
            if (filter.getSecurityLevel() != null) {
                predicates.add(hasSecurityLevel(filter.getSecurityLevel()).toPredicate(root, query, cb));
            }
            if (filter.getActive() != null) {
                predicates.add(isActive(filter.getActive()).toPredicate(root, query, cb));
            }
            if (filter.getRoleId() != null) {
                predicates.add(hasRoleId(filter.getRoleId()).toPredicate(root, query, cb));
            }
            if (filter.getActivationDateFrom() != null) {
                predicates.add(activationDateFrom(filter.getActivationDateFrom()).toPredicate(root, query, cb));
            }
            if (filter.getActivationDateTo() != null) {
                predicates.add(activationDateTo(filter.getActivationDateTo()).toPredicate(root, query, cb));
            }
            if (filter.getExpirationDateFrom() != null) {
                predicates.add(expirationDateFrom(filter.getExpirationDateFrom()).toPredicate(root, query, cb));
            }
            if (filter.getExpirationDateTo() != null) {
                predicates.add(expirationDateTo(filter.getExpirationDateTo()).toPredicate(root, query, cb));
            }
            if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
                predicates.add(searchTerm(filter.getSearchTerm()).toPredicate(root, query, cb));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
