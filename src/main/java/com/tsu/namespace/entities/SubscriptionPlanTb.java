package com.tsu.namespace.entities;

import com.tsu.namespace.entities.id.SubscriptionPlanId;
import com.tsu.workspace.config.DurationUnit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "subscription_plan")
public class SubscriptionPlanTb {

    @EmbeddedId
    private SubscriptionPlanId id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "duration_unit", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private DurationUnit durationUnit;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "max_user", nullable = false)
    private Integer maxUser;

}
