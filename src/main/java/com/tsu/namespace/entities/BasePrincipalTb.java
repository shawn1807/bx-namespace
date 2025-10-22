package com.tsu.namespace.entities;

import com.tsu.common.api.PrincipalType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "base_principal")
public class BasePrincipalTb {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PrincipalType type;


}
