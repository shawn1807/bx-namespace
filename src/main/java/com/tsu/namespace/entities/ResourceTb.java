package com.tsu.namespace.entities;

import com.tsu.common.jpa.Jsonb;
import com.tsu.common.jpa.JsonbType;
import com.tsu.namespace.api.ResourceType;
import com.tsu.namespace.entities.id.ResourceId;
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
@Table(name = "resource")
public class ResourceTb {

    @EmbeddedId
    private ResourceId id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ResourceType type;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "location")
    private String location;

    @Column(name = "timezone")
    private String timezone;

    @Type(JsonbType.class)
    @Column(name = "meta", columnDefinition = "jsonb")
    private Jsonb meta;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
