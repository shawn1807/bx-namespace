package com.tsu.namespace.record;

import com.tsu.namespace.entities.NumberSequenceTb;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Consumer;


@RequiredArgsConstructor
public class NumberSeqRecord {

    private final NumberSequenceTb tb;

    private final Consumer<NumberSequenceTb> persist;

    public void persist() {
        persist.accept(tb);
    }


    public int increment() {
        int next = Optional.ofNullable(tb.getCurrentSeq()).orElse(0) + 1;
        tb.setCurrentSeq(next);
        return next;
    }
}
