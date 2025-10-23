package com.tsu.namespace.entities;

import com.tsu.common.jpa.Jsonb;
import com.tsu.common.jpa.JsonbType;
import com.tsu.namespace.entities.id.NamespaceRoleId;
import com.tsu.namespace.entities.id.PlaceId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Enhanced Place entity with composite ID and full PostGIS support.
 * Supports hierarchy, geo-spatial queries, and multi-language names.
 */
@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "place")
public class PlaceTb {

    @EmbeddedId
    private PlaceId id;

    // Hierarchy
    @Column(name = "parent_id")
    private UUID parentId;

    // Classification
    @Column(name = "type", nullable = false)
    private String type;  // country, region, city, building, floor, room, poi, etc.

    @Column(name = "iso_country", length = 2)
    private String isoCountry;  // ISO 3166-1 alpha-2

    // Address components
    @Column(name = "country")
    private String country;

    @Column(name = "county")
    private String county;

    @Column(name = "city")
    private String city;

    @Column(name = "building")
    private String building;

    @Column(name = "address")
    private String address;

    @Column(name = "post_code", length = 20)
    private String postCode;

    // Primary name
    @Column(name = "name", nullable = false)
    private String name;

    // Geo coordinates
    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    // PostGIS geometry (WGS84 SRID 4326)
    @Column(name = "center", columnDefinition = "geography(Point,4326)")
    private Point center;  // For distance/radius queries

    @Column(name = "bbox", columnDefinition = "geometry(Polygon,4326)")
    private Polygon bbox;  // Bounding box for area-based filtering

    // Additional data
    @Type(JsonbType.class)
    @Column(name = "props", columnDefinition = "jsonb")
    private Jsonb props;

    @Column(name = "notes")
    private String notes;

    // Status
    @Column(name = "active", nullable = false)
    private boolean active = true;

    // Audit fields (following standard naming convention)
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_by", nullable = false)
    private UUID modifiedBy;

    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;
}
