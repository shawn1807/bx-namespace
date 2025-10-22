package com.tsu.namespace.entities;

import com.tsu.namespace.entities.id.NumberId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "number")
public class NumberTb {

    @EmbeddedId
    private NumberId id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "prefix")
    private String prefix;

    @Column(name = "suffix")
    private String suffix;

    @Column(name = "length", nullable = false)
    private int length;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

}
