package com.tsu.namespace.entities;

import com.tsu.namespace.entities.id.SubscriptionHistoryId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "subscription_history")
public class SubscriptionHistoryTb {

    @EmbeddedId
    private SubscriptionHistoryId id;

    @Column(name = "provider_id", nullable = false)
    private UUID providerId;

    @Column(name = "plan", nullable = false)
    private String  plan;

    @Column(name = "activation_date", nullable = false)
    private LocalDate activationDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

}
