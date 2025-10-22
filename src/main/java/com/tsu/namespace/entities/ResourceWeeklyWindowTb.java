package com.tsu.namespace.entities;

import com.tsu.namespace.entities.id.ResourceWeeklyWindowId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "resource_weekly_window")
public class ResourceWeeklyWindowTb {

    @EmbeddedId
    private ResourceWeeklyWindowId id;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "namespace_id", referencedColumnName = "namespace_id", insertable = false, updatable = false),
        @JoinColumn(name = "resource_id", referencedColumnName = "id")
    })
    private ResourceTb resource;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    /**
     * Day of week: 1-7 (Monday-Sunday)
     */
    @Column(name = "dow", nullable = false)
    private Integer dow;

    @Column(name = "start_local", nullable = false)
    private LocalTime startLocal;

    @Column(name = "end_local", nullable = false)
    private LocalTime endLocal;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
