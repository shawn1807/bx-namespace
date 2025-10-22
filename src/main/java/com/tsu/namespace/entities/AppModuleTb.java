package com.tsu.namespace.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "app_module")
public class AppModuleTb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "build", nullable = false)
    private int build;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "build_package", nullable = false)
    private String buildPackage;

    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;
}
