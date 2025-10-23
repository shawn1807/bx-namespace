package com.tsu.namespace.entities;

import com.tsu.namespace.entities.id.PlaceAltNameId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for alternate place names (translations, abbreviations, historical names).
 */
@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "place_alt_name")
@IdClass(PlaceAltNameId.class)
public class PlaceAltNameTb {

    @Id
    @Column(name = "namespace_id", nullable = false)
    private UUID namespaceId;

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false)
    private UUID placeId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "lang", length = 10)
    private String lang;  // ISO 639-1 language code

    @Column(name = "type")
    private String type;  // official, colloquial, historic, abbreviation

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
}
