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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "user_base")
public class UserBaseTb {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "display_name", nullable = false, unique = true)
    private String displayName;


    @Column(name = "first_name", nullable = true)
    private String firstName;

    @Column(name = "last_name", nullable = true)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "image_url")
    private String imageUrl;

    @Type(JsonbType.class)
    @Column(name = "profile", columnDefinition = "jsonb")
    private Jsonb profile;

    @Type(JsonbType.class)
    @Column(name = "preference", columnDefinition = "jsonb")
    private Jsonb preference;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    // Locale and regional settings (nullable - falls back to namespace settings)
    @Column(name = "currency_code", length = 3)
    private String currencyCode;  // ISO 4217 override

    @Column(name = "language_tag")
    private String languageTag;  // IETF BCP47 override

    @Column(name = "timezone_id")
    private String timezoneId;  // IANA timezone override

    @Column(name = "date_pattern")
    private String datePattern;  // Date format override

    @Column(name = "time_pattern")
    private String timePattern;  // Time format override

    @Column(name = "datetime_pattern")
    private String datetimePattern;  // DateTime format override

    // Audit fields
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_by", nullable = false)
    private UUID modifiedBy;

    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;
}
