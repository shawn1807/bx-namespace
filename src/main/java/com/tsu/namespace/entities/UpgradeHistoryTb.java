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
@Table(name = "upgrade_history")
public class UpgradeHistoryTb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "module")
    private String module;

    @Column(name = "version")
    private String version;

    @Column(name = "build")
    private Integer build;

    @Column(name = "description")
    private String description;

    @Column(name = "upgrade_date")
    private LocalDateTime upgradeDate;
}
