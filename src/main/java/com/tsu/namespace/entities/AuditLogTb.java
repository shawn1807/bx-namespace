package com.tsu.namespace.entities;

import com.tsu.common.jpa.Jsonb;
import com.tsu.common.jpa.JsonbType;
import com.tsu.namespace.entities.id.AuditLogId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "audit_log")
public class AuditLogTb {

    @EmbeddedId
    private AuditLogId id;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "entity", nullable = false)
    private String entity;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "at", nullable = false)
    private LocalDateTime at;

    @Type(JsonbType.class)
    @Column(name = "diff", columnDefinition = "jsonb")
    private Jsonb diff;
}
