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
    @Column(name = "preferences", columnDefinition = "jsonb")
    private Jsonb preferences;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "language_tag")
    private String languageTag;  // IETF BCP47 override

    @Column(name = "timezone_id")
    private String timezoneId;  // IANA timezone override

    @Column(name = "date_pattern")
    private String datePattern;  // Date format override

    @Column(name = "datetime_pattern")
    private String datetimePattern;  // DateTime format override

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;
}
