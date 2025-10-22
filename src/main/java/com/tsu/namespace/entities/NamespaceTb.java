package com.tsu.namespace.entities;

import com.tsu.auth.api.AccessLevel;
import com.tsu.common.jpa.Jsonb;
import com.tsu.common.jpa.JsonbType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "namespace")
public class NamespaceTb {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "uri", nullable = false)
    private String uri;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "bucket", nullable = false)
    private String bucket;

    @Type(JsonbType.class)
    @Column(name = "props", columnDefinition = "jsonb")
    private Jsonb props;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "description")
    private String description;

    @Column(name = "website", nullable = false)
    private String website;

    @Column(name = "logo_image_url", nullable = false)
    private String logoImageUrl;

    @Column(name = "background_image_url", nullable = false)
    private String backgroundImageUrl;

    @Column(name = "access_level")
    private AccessLevel accessLevel;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_by", nullable = false)
    private UUID modifiedBy;

    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;
}
