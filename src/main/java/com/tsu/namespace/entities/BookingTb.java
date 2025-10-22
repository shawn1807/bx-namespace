package com.tsu.namespace.entities;

import com.tsu.namespace.api.BookingStatus;
import com.tsu.namespace.entities.id.BookingId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "booking")
public class BookingTb {

    @EmbeddedId
    private BookingId id;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "namespace_id", referencedColumnName = "namespace_id", insertable = false, updatable = false),
        @JoinColumn(name = "resource_id", referencedColumnName = "id")
    })
    private ResourceTb resource;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    /**
     * Reference to external user directory
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "title")
    private String title;

    @Column(name = "notes")
    private String notes;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Note: slot field is a generated column (tstzrange) in PostgreSQL
    // Not mapped in JPA as it's automatically maintained by the database
}
