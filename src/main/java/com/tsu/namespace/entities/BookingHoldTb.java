package com.tsu.namespace.entities;

import com.tsu.namespace.entities.id.BookingHoldId;
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
@Table(name = "booking_hold")
public class BookingHoldTb {

    @EmbeddedId
    private BookingHoldId id;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "namespace_id", referencedColumnName = "namespace_id", insertable = false, updatable = false),
        @JoinColumn(name = "resource_id", referencedColumnName = "id")
    })
    private ResourceTb resource;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Note: slot field is a generated column (tstzrange) in PostgreSQL
    // Not mapped in JPA as it's automatically maintained by the database
}
