package com.tsu.namespace.entities;

import com.tsu.auth.api.AuthProvider;
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
@Table(name = "login")
public class LoginTb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private AuthProvider provider;

    @Column(name = "auth_id", nullable = false, unique = true)
    private String authId;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Type(JsonbType.class)
    @Column(name = "props", columnDefinition = "jsonb")
    private Jsonb props;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;


}
