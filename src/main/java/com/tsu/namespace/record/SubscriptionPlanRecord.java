package com.tsu.namespace.record;

import com.tsu.namespace.entities.SubscriptionPlanTb;
import com.tsu.workspace.config.DurationUnit;
import com.tsu.namespace.val.SubscriptionPlanVal;
import lombok.RequiredArgsConstructor;

import java.time.Period;
import java.util.Optional;

@RequiredArgsConstructor
public class SubscriptionPlanRecord {

    private final SubscriptionPlanTb tb;


    public String getName() {
        return tb.getName();
    }

    public String getDescription() {
        return tb.getDescription();
    }

    public int getMaxUser() {
        return tb.getMaxUser();
    }

    public Period getPeriod() {
        int duration = Optional.ofNullable(tb.getDuration()).orElse(0);
        return Optional.ofNullable(tb.getDurationUnit())
                .map(unit -> switch (unit) {
                    case DurationUnit.days -> Period.ofDays(duration);
                    case DurationUnit.weeks -> Period.ofWeeks(duration);
                    case DurationUnit.months -> Period.ofMonths(duration);
                    case DurationUnit.years -> Period.ofYears(duration);
                })
                .orElse(Period.ZERO);
    }


    public SubscriptionPlanVal getValue() {
        return new SubscriptionPlanVal(tb.getId().getId(), tb.getName(), tb.getDescription(), getPeriod(), tb.getMaxUser());
    }
}
