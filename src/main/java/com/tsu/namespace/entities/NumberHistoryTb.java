package com.tsu.namespace.entities;

import com.tsu.namespace.entities.id.NumberHistoryId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "number_history")
public class NumberHistoryTb {

    @EmbeddedId
    private NumberHistoryId id;

    @Column(name = "number", nullable = false)
    private String number;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

}
