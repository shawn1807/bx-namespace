package com.tsu.namespace.entities;

import com.tsu.namespace.entities.id.NumberSequenceId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "number_sequence")
public class NumberSequenceTb {

    @EmbeddedId
    private NumberSequenceId id;

    @Column(name = "prefix")
    private String prefix;

    @Column(name = "current_seq")
    private Integer currentSeq;

    @Column(name = "version")
    @Version
    private int version;

}
