package com.tsu.namespace.entities;

import com.tsu.common.jpa.Jsonb;
import com.tsu.common.jpa.JsonbType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import org.locationtech.jts.geom.Point;

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "place")
public class PlaceTb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "notes")
    private String notes;

    @Column(name = "country", nullable = false, length = 2)
    private String country;

    @Column(name = "county", nullable = false)
    private String county;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "building")
    private String building;

    @Column(name = "post_code", length = 20)
    private String postCode;

    @Column(name = "address")
    private String address;

    @Column(name = "lat")
    private double lat;

    @Column(name = "lng")
    private double lng;

    @Column(name = "geom", columnDefinition = "geometry(Point,4326)")
    private Point geom;

    @Type(JsonbType.class)
    @Column(name = "props", columnDefinition = "jsonb")
    private Jsonb props;



}
