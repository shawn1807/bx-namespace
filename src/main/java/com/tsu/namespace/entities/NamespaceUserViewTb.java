package com.tsu.namespace.entities;

import com.tsu.namespace.api.NamespaceUserType;
import com.tsu.common.jpa.Jsonb;
import com.tsu.common.jpa.JsonbType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for namespace_user_mv materialized view
 * This view joins namespace_user, user_base, and namespace tables for efficient querying
 */
@Data
@ToString
@EqualsAndHashCode
@Entity
@Immutable
@Table(name = "namespace_user_mv")
public class NamespaceUserViewTb {

    @Id
    @Column(name = "id")
    private Integer id;

    // namespace_user core fields
    @Column(name = "namespace_id", nullable = false)
    private UUID namespaceId;

    @Column(name = "principal_id", nullable = false)
    private UUID principalId;

    @Column(name = "role")
    private String role;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NamespaceUserType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "security_level", nullable = false)
    private SecurityClass securityLevel;

    @Type(JsonbType.class)
    @Column(name = "permissions", columnDefinition = "jsonb")
    private Jsonb permissions;

    @Column(name = "entry_id")
    private Long entryId;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "activation_date")
    private LocalDate activationDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_by", nullable = false)
    private UUID modifiedBy;

    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;

    // user_base fields
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "image_url")
    private String imageUrl;

    @Type(JsonbType.class)
    @Column(name = "profile", columnDefinition = "jsonb")
    private Jsonb profile;

    @Type(JsonbType.class)
    @Column(name = "preference", columnDefinition = "jsonb")
    private Jsonb preference;

    @Column(name = "user_active", nullable = false)
    private boolean userActive;

    @Column(name = "user_expiration_date")
    private LocalDate userExpirationDate;


}
