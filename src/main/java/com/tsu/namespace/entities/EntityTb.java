package com.tsu.namespace.entities;

import com.tsu.namespace.entities.id.EntityId;
import com.tsu.namespace.entities.id.EntityTypeId;
import com.tsu.namespace.entities.id.NamespaceUserId;
import com.tsu.common.jpa.Jsonb;
import com.tsu.common.jpa.JsonbType;
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
@Table(name = "entity")
public class EntityTb {

    @EmbeddedId
    private EntityId id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "namespace_id", referencedColumnName = "namespace_id", insertable = false, updatable = false),
        @JoinColumn(name = "type_id", referencedColumnName = "id")
    })
    private EntityTypeTb entityType;

    @Column(name = "type_id")
    private Integer typeId;

    @Column(name = "clazz")
    private String clazz;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Type(JsonbType.class)
    @Column(name = "profile", columnDefinition = "jsonb")
    private Jsonb profile;

    @Column(name = "primary_place_id")
    private Integer primaryPlaceId;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "namespace_id", referencedColumnName = "namespace_id", insertable = false, updatable = false),
        @JoinColumn(name = "parent_id", referencedColumnName = "id")
    })
    private EntityTb parent;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "active")
    private boolean active;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_by", nullable = false)
    private Integer modifiedBy;

    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;
}
