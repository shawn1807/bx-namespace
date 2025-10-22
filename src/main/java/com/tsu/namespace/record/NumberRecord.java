package com.tsu.namespace.record;

import com.tsu.namespace.entities.NumberTb;
import com.tsu.namespace.val.NumberSeqVal;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;


@RequiredArgsConstructor
public class NumberRecord {

    private final NumberTb tb;

    private final Consumer<NumberTb> persist;

    public void persist() {
        persist.accept(tb);
    }


    public String getPrefix() {
        return tb.getPrefix();
    }

    public String getSuffix() {
        return tb.getSuffix();
    }

    public int getLength() {
        return tb.getLength();
    }

    public NumberSeqVal getValue() {
        return new NumberSeqVal(tb.getName(), tb.getType(), tb.getPrefix(), tb.getSuffix(), tb.getLength(),
                tb.getCreatedBy(), tb.getCreatedDate());
    }
}
