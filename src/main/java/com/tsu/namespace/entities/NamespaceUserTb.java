package com.tsu.namespace.entities;

import com.tsu.base.api.NamespaceUserType;
import com.tsu.namespace.api.SecurityClass;
import com.tsu.namespace.entities.id.NamespaceRoleId;
import com.tsu.namespace.entities.id.NamespaceUserId;
import com.tsu.base.val.NspUsrVal;
import com.tsu.common.jpa.Jsonb;
import com.tsu.common.jpa.JsonbType;
import jakarta.persistence.*;
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
@Table(name = "namespace_user")
@SqlResultSetMapping(
        name = "UserJoinMapping",
        classes = @ConstructorResult(
                targetClass = NspUsrVal.class,
                columns = {
                    @ColumnResult(name = "", type = UUID.class),
                    @ColumnResult(name = "", type = UUID.class),
                    @ColumnResult(name = "", type = UUID.class),
                    @ColumnResult(name = "", type = UUID.class),
                    @ColumnResult(name = "", type = UUID.class),
                    @ColumnResult(name = "", type = UUID.class),
                }
        )
)

public class NamespaceUserTb {

    @EmbeddedId
    private NamespaceUserId id;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "namespace_id", referencedColumnName = "namespace_id", insertable = false, updatable = false),
        @JoinColumn(name = "role_id", referencedColumnName = "id")
    })
    private NamespaceRoleTb role;

    @Column(name = "principal_id", nullable = false)
    private UUID principalId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NamespaceUserType type;

    @Type(JsonbType.class)
    @Column(name = "permissions", columnDefinition = "jsonb")
    private Jsonb permissions;

    @Column(name = "entry_id")
    private UUID entryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "security_level", nullable = false)
    private SecurityClass securityLevel;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "activation_date", nullable = false)
    private LocalDate activationDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "approved_date", nullable = false)
    private LocalDateTime approvedDate;

    @Column(name = "approved_by", nullable = false)
    private UUID approvedBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "modified_by", nullable = false)
    private UUID modifiedBy;

    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;
}
