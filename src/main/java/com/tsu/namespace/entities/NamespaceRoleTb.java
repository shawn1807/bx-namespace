package com.tsu.namespace.entities;

import com.tsu.namespace.entities.id.NamespaceRoleId;
import com.tsu.namespace.entities.id.NamespaceUserId;
import com.tsu.common.jpa.Jsonb;
import com.tsu.common.jpa.JsonbType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "namespace_role")
public class NamespaceRoleTb {

    @EmbeddedId
    private NamespaceRoleId id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Type(JsonbType.class)
    @Column(name = "permissions", columnDefinition = "jsonb")
    private Jsonb permissions;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_by", nullable = false)
    private Integer modifiedBy;

    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;
}
