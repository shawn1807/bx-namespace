package com.tsu.namespace.repo.spec;

import com.tsu.namespace.api.BookingStatus;
import com.tsu.namespace.entities.BookingTb;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Specification for complex Booking queries.
 * Provides type-safe, composable query building for booking search and filtering.
 */
@Slf4j
public class BookingSpecification {

    /**
     * Specification for bookings within a namespace.
     * This should always be included for multi-tenant isolation.
     */
    public static Specification<BookingTb> hasNamespaceId(UUID namespaceId) {
        return (root, query, cb) -> cb.equal(root.get("id").get("namespaceId"), namespaceId);
    }

    /**
     * Specification for bookings for a specific user.
     */
    public static Specification<BookingTb> hasUserId(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    /**
     * Specification for bookings for a specific resource.
     */
    public static Specification<BookingTb> hasResourceId(UUID resourceId) {
        return (root, query, cb) -> cb.equal(root.get("resourceId"), resourceId);
    }

    /**
     * Specification for bookings with a specific status.
     */
    public static Specification<BookingTb> hasStatus(BookingStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /**
     * Specification for bookings with any of the given statuses.
     */
    public static Specification<BookingTb> hasAnyStatus(List<BookingStatus> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    /**
     * Specification for active (non-deleted) bookings.
     */
    public static Specification<BookingTb> isNotDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedDate"));
    }

    /**
     * Specification for deleted bookings.
     */
    public static Specification<BookingTb> isDeleted() {
        return (root, query, cb) -> cb.isNotNull(root.get("deletedDate"));
    }

    /**
     * Specification for bookings starting after a specific date/time.
     */
    public static Specification<BookingTb> startsAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startAt"), dateTime);
    }

    /**
     * Specification for bookings starting before a specific date/time.
     */
    public static Specification<BookingTb> startsBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startAt"), dateTime);
    }

    /**
     * Specification for bookings ending after a specific date/time.
     */
    public static Specification<BookingTb> endsAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("endAt"), dateTime);
    }

    /**
     * Specification for bookings ending before a specific date/time.
     */
    public static Specification<BookingTb> endsBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("endAt"), dateTime);
    }

    /**
     * Specification for bookings within a time range (start and end within range).
     */
    public static Specification<BookingTb> withinRange(LocalDateTime startRange, LocalDateTime endRange) {
        return (root, query, cb) -> cb.and(
            cb.greaterThanOrEqualTo(root.get("startAt"), startRange),
            cb.lessThanOrEqualTo(root.get("endAt"), endRange)
        );
    }

    /**
     * Specification for bookings that overlap with a given time range.
     * A booking overlaps if: booking.startAt < rangeEnd AND booking.endAt > rangeStart
     */
    public static Specification<BookingTb> overlapsRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return (root, query, cb) -> cb.and(
            cb.lessThan(root.get("startAt"), rangeEnd),
            cb.greaterThan(root.get("endAt"), rangeStart)
        );
    }

    /**
     * Specification for bookings created by a specific user.
     */
    public static Specification<BookingTb> createdBy(UUID createdBy) {
        return (root, query, cb) -> cb.equal(root.get("createdBy"), createdBy);
    }

    /**
     * Specification for bookings created after a specific date/time.
     */
    public static Specification<BookingTb> createdAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdDate"), dateTime);
    }

    /**
     * Specification for bookings created before a specific date/time.
     */
    public static Specification<BookingTb> createdBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdDate"), dateTime);
    }

    /**
     * Specification for title search (case-insensitive contains).
     */
    public static Specification<BookingTb> titleContains(String text) {
        return (root, query, cb) -> cb.like(
            cb.lower(root.get("title")),
            "%" + text.toLowerCase() + "%"
        );
    }

    /**
     * Specification for notes search (case-insensitive contains).
     */
    public static Specification<BookingTb> notesContain(String text) {
        return (root, query, cb) -> cb.like(
            cb.lower(root.get("notes")),
            "%" + text.toLowerCase() + "%"
        );
    }

    /**
     * Specification for text search in title or notes.
     */
    public static Specification<BookingTb> textSearch(String text) {
        return (root, query, cb) -> cb.or(
            cb.like(cb.lower(root.get("title")), "%" + text.toLowerCase() + "%"),
            cb.like(cb.lower(root.get("notes")), "%" + text.toLowerCase() + "%")
        );
    }

    /**
     * Specification for confirmed or tentative bookings (active statuses).
     */
    public static Specification<BookingTb> isActiveStatus() {
        return (root, query, cb) -> root.get("status").in(
            List.of(BookingStatus.CONFIRMED, BookingStatus.TENTATIVE)
        );
    }

    /**
     * Specification for upcoming bookings (starts after now).
     */
    public static Specification<BookingTb> isUpcoming() {
        return startsAfter(LocalDateTime.now());
    }

    /**
     * Specification for past bookings (ends before now).
     */
    public static Specification<BookingTb> isPast() {
        return endsBefore(LocalDateTime.now());
    }

    /**
     * Specification for current/ongoing bookings (started but not ended).
     */
    public static Specification<BookingTb> isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return (root, query, cb) -> cb.and(
            cb.lessThanOrEqualTo(root.get("startAt"), now),
            cb.greaterThanOrEqualTo(root.get("endAt"), now)
        );
    }

    /**
     * Build a complex specification from a BookingFilter object.
     * This method demonstrates how to combine multiple specifications.
     */
    public static Specification<BookingTb> fromFilter(UUID namespaceId, BookingFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Namespace isolation (always required)
            predicates.add(cb.equal(root.get("id").get("namespaceId"), namespaceId));

            // User filter
            if (filter.getUserId() != null) {
                predicates.add(cb.equal(root.get("userId"), filter.getUserId()));
            }

            // Resource filter
            if (filter.getResourceId() != null) {
                predicates.add(cb.equal(root.get("resourceId"), filter.getResourceId()));
            }

            // Status filter
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Multiple statuses filter
            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(filter.getStatuses()));
            }

            // Deleted filter (default to non-deleted)
            if (filter.getIncludeDeleted() != null && !filter.getIncludeDeleted()) {
                predicates.add(cb.isNull(root.get("deletedDate")));
            } else if (filter.getIncludeDeleted() == null) {
                // Default behavior: exclude deleted
                predicates.add(cb.isNull(root.get("deletedDate")));
            }

            // Date range filters
            if (filter.getStartAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startAt"), filter.getStartAfter()));
            }
            if (filter.getStartBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startAt"), filter.getStartBefore()));
            }
            if (filter.getEndAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("endAt"), filter.getEndAfter()));
            }
            if (filter.getEndBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endAt"), filter.getEndBefore()));
            }

            // Overlap filter
            if (filter.getOverlapStart() != null && filter.getOverlapEnd() != null) {
                predicates.add(cb.and(
                    cb.lessThan(root.get("startAt"), filter.getOverlapEnd()),
                    cb.greaterThan(root.get("endAt"), filter.getOverlapStart())
                ));
            }

            // Created by filter
            if (filter.getCreatedBy() != null) {
                predicates.add(cb.equal(root.get("createdBy"), filter.getCreatedBy()));
            }

            // Created date range
            if (filter.getCreatedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"), filter.getCreatedAfter()));
            }
            if (filter.getCreatedBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdDate"), filter.getCreatedBefore()));
            }

            // Text search
            if (filter.getSearchText() != null && !filter.getSearchText().isEmpty()) {
                String searchPattern = "%" + filter.getSearchText().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), searchPattern),
                    cb.like(cb.lower(root.get("notes")), searchPattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter object for booking queries.
     * This is a simple DTO to hold all possible filter parameters.
     */
    public static class BookingFilter {
        private UUID userId;
        private UUID resourceId;
        private BookingStatus status;
        private List<BookingStatus> statuses;
        private Boolean includeDeleted;
        private LocalDateTime startAfter;
        private LocalDateTime startBefore;
        private LocalDateTime endAfter;
        private LocalDateTime endBefore;
        private LocalDateTime overlapStart;
        private LocalDateTime overlapEnd;
        private UUID createdBy;
        private LocalDateTime createdAfter;
        private LocalDateTime createdBefore;
        private String searchText;

        // Getters and setters

        public UUID getUserId() {
            return userId;
        }

        public BookingFilter setUserId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public UUID getResourceId() {
            return resourceId;
        }

        public BookingFilter setResourceId(UUID resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public BookingStatus getStatus() {
            return status;
        }

        public BookingFilter setStatus(BookingStatus status) {
            this.status = status;
            return this;
        }

        public List<BookingStatus> getStatuses() {
            return statuses;
        }

        public BookingFilter setStatuses(List<BookingStatus> statuses) {
            this.statuses = statuses;
            return this;
        }

        public Boolean getIncludeDeleted() {
            return includeDeleted;
        }

        public BookingFilter setIncludeDeleted(Boolean includeDeleted) {
            this.includeDeleted = includeDeleted;
            return this;
        }

        public LocalDateTime getStartAfter() {
            return startAfter;
        }

        public BookingFilter setStartAfter(LocalDateTime startAfter) {
            this.startAfter = startAfter;
            return this;
        }

        public LocalDateTime getStartBefore() {
            return startBefore;
        }

        public BookingFilter setStartBefore(LocalDateTime startBefore) {
            this.startBefore = startBefore;
            return this;
        }

        public LocalDateTime getEndAfter() {
            return endAfter;
        }

        public BookingFilter setEndAfter(LocalDateTime endAfter) {
            this.endAfter = endAfter;
            return this;
        }

        public LocalDateTime getEndBefore() {
            return endBefore;
        }

        public BookingFilter setEndBefore(LocalDateTime endBefore) {
            this.endBefore = endBefore;
            return this;
        }

        public LocalDateTime getOverlapStart() {
            return overlapStart;
        }

        public BookingFilter setOverlapStart(LocalDateTime overlapStart) {
            this.overlapStart = overlapStart;
            return this;
        }

        public LocalDateTime getOverlapEnd() {
            return overlapEnd;
        }

        public BookingFilter setOverlapEnd(LocalDateTime overlapEnd) {
            this.overlapEnd = overlapEnd;
            return this;
        }

        public UUID getCreatedBy() {
            return createdBy;
        }

        public BookingFilter setCreatedBy(UUID createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public LocalDateTime getCreatedAfter() {
            return createdAfter;
        }

        public BookingFilter setCreatedAfter(LocalDateTime createdAfter) {
            this.createdAfter = createdAfter;
            return this;
        }

        public LocalDateTime getCreatedBefore() {
            return createdBefore;
        }

        public BookingFilter setCreatedBefore(LocalDateTime createdBefore) {
            this.createdBefore = createdBefore;
            return this;
        }

        public String getSearchText() {
            return searchText;
        }

        public BookingFilter setSearchText(String searchText) {
            this.searchText = searchText;
            return this;
        }

        // Builder-style methods for easier usage
        public static BookingFilter builder() {
            return new BookingFilter();
        }

        public BookingFilter forUser(UUID userId) {
            this.userId = userId;
            return this;
        }

        public BookingFilter forResource(UUID resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public BookingFilter withStatus(BookingStatus status) {
            this.status = status;
            return this;
        }

        public BookingFilter withStatuses(List<BookingStatus> statuses) {
            this.statuses = statuses;
            return this;
        }

        public BookingFilter activeOnly() {
            this.includeDeleted = false;
            return this;
        }

        public BookingFilter overlapping(LocalDateTime start, LocalDateTime end) {
            this.overlapStart = start;
            this.overlapEnd = end;
            return this;
        }

        public BookingFilter searchText(String text) {
            this.searchText = text;
            return this;
        }
    }
}
