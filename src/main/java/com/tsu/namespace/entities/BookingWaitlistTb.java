package com.tsu.namespace.entities;

import com.tsu.namespace.entities.id.BookingWaitlistId;
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
@Table(name = "booking_waitlist")
public class BookingWaitlistTb {

    @EmbeddedId
    private BookingWaitlistId id;

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

    @Column(name = "desired_start", nullable = false)
    private LocalDateTime desiredStart;

    @Column(name = "desired_end", nullable = false)
    private LocalDateTime desiredEnd;

    @Column(name = "priority", nullable = false)
    private Integer priority = 100;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
