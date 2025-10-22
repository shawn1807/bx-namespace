package com.tsu.namespace.api.namespace;

import com.tsu.namespace.api.NextNumberRequest;
import com.tsu.namespace.api.NumberSeq;
import com.tsu.namespace.helper.NumberDbHelper;
import com.tsu.namespace.record.NumberRecord;
import com.tsu.namespace.record.NumberSeqRecord;
import com.tsu.namespace.val.NumberSeqVal;
import com.tsu.auth.security.NamespaceContext;
import com.tsu.util.NumberPattern;
import com.tsu.util.TextUtils;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class NumberSeqImpl implements NumberSeq {

    @ToString.Include
    private final NumberRecord record;
    private final NumberDbHelper dbHelper;
    private final NamespaceContext context;


    @Override
    public String next(NextNumberRequest request) {
        LocalDateTime baseDate = Optional.ofNullable(request.getBaseDate()).orElse(LocalDateTime.now());
        String prefix = NumberPattern.compile(record.getPrefix()).replace(baseDate, request.getParams());
        Integer next = getNext(prefix);
        StringBuilder value = new StringBuilder(prefix);
        String suffix = NumberPattern.compile(Optional.ofNullable(record.getSuffix()).orElse("")).replace(baseDate, request.getParams());
        String nextValue = value.append(TextUtils.paddingHead(record.getLength(), String.valueOf(next), "0")) //
                .append(suffix) //
                .toString();
        dbHelper.addHistory(context.getNamespaceId(), nextValue, context);
        return nextValue;
    }

    @Override
    public NumberSeqVal getValue() {
        return record.getValue();
    }

    private int getNext(String prefix) {
        NumberSeqRecord numberSeqRecord = dbHelper.findNumberSeqByNamespaceIdAndPrefix(context.getNamespaceId(), prefix)
                .orElseGet(() -> dbHelper.addNumberSequence(context.getNamespaceId(), prefix));
        int next = numberSeqRecord.increment();
        numberSeqRecord.persist();
        return next;
    }


}
