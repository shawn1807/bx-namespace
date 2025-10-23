package com.tsu.namespace.record;

import com.tsu.namespace.api.TimeSlot;
import com.tsu.namespace.val.TimeSlotVal;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Record wrapper for TimeSlot data.
 */
@ToString
@RequiredArgsConstructor
public class TimeSlotRecord implements TimeSlot {

    private final LocalDateTime startAt;
    private final LocalDateTime endAt;

    public TimeSlotVal getValue() {
        return new TimeSlotVal(startAt, endAt);
    }

    @Override
    public LocalDateTime getStartAt() {
        return startAt;
    }

    @Override
    public LocalDateTime getEndAt() {
        return endAt;
    }

    @Override
    public Integer getDurationMinutes() {
        return (int) java.time.Duration.between(startAt, endAt).toMinutes();
    }
}
