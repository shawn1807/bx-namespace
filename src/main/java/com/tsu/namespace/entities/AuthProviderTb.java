package com.tsu.namespace.entities;

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

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "auth_provider")
public class AuthProviderTb {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Type(JsonbType.class)
    @Column(name = "props", columnDefinition = "jsonb")
    private Jsonb props;


}
