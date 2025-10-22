package com.tsu.namespace.entities;

import com.tsu.common.jpa.Jsonb;
import com.tsu.common.jpa.JsonbType;
import com.tsu.namespace.entities.id.EventAuditId;
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
@Table(name = "event_audit")
public class EventAuditTb {

    @EmbeddedId
    private EventAuditId id;

    @Column(name = "entry_id", nullable = false)
    private UUID entryId;

    @Column(name = "action", nullable = false)
    private String action;

    @Type(JsonbType.class)
    @Column(name = "params", columnDefinition = "jsonb")
    private Jsonb params;

    @Column(name = "txid", nullable = false)
    private String txid;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

}
