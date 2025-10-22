package com.tsu.namespace.entities;

import com.tsu.namespace.entities.id.EntityTypeId;
import com.tsu.namespace.entities.id.NamespaceUserId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "entity_type")
public class EntityTypeTb {

    @EmbeddedId
    private EntityTypeId id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

}
