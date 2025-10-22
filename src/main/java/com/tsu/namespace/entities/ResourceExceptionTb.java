package com.tsu.namespace.entities;

import com.tsu.namespace.entities.id.ResourceExceptionId;
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
@Table(name = "resource_exception")
public class ResourceExceptionTb {

    @EmbeddedId
    private ResourceExceptionId id;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "namespace_id", referencedColumnName = "namespace_id", insertable = false, updatable = false),
        @JoinColumn(name = "resource_id", referencedColumnName = "id")
    })
    private ResourceTb resource;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Note: span field is a generated column (tstzrange) in PostgreSQL
    // Not mapped in JPA as it's automatically maintained by the database
}
