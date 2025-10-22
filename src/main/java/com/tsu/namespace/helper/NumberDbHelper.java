package com.tsu.namespace.helper;

import com.tsu.auth.security.NamespaceContext;
import com.tsu.namespace.entities.NumberHistoryTb;
import com.tsu.namespace.entities.NumberSequenceTb;
import com.tsu.namespace.entities.NumberTb;
import com.tsu.namespace.entities.id.NumberHistoryId;
import com.tsu.namespace.entities.id.NumberId;
import com.tsu.namespace.entities.id.NumberSequenceId;
import com.tsu.namespace.record.NumberRecord;
import com.tsu.namespace.record.NumberSeqRecord;
import com.tsu.namespace.repo.NumberHistoryRepository;
import com.tsu.namespace.repo.NumberRepository;
import com.tsu.namespace.repo.NumberSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class NumberDbHelper {

    private final NumberRepository numberRepository;
    private final NumberSequenceRepository sequenceRepository;
    private final NumberHistoryRepository historyRepository;


    public Optional<NumberRecord> findNumberByNamespaceIdAndName(UUID id, String name) {
        return numberRepository.findByIdNamespaceIdAndName(id, name)
                .map(this::build);
    }

    public Stream<NumberRecord> findNumberByNamespaceId(UUID id) {
        return numberRepository.findByIdNamespaceId(id)
                .map(this::build);
    }

    public Stream<NumberRecord> findNumberByNamespaceIdAndType(UUID id, String type) {
        return numberRepository.findByIdNamespaceIdAndType(id, type)
                .map(this::build);
    }

    public Optional<NumberSeqRecord> findNumberSeqByNamespaceIdAndPrefix(UUID id, String prefix) {
        return sequenceRepository.findByIdNamespaceIdAndPrefix(id, prefix)
                .map(this::build);
    }

    private NumberRecord build(NumberTb tb) {
        return new NumberRecord(tb, numberRepository::save);
    }

    private NumberSeqRecord build(NumberSequenceTb tb) {
        return new NumberSeqRecord(tb, sequenceRepository::save);
    }

    public NumberSeqRecord addNumberSequence(UUID namespaceId, String prefix) {
        NumberSequenceTb s = new NumberSequenceTb();
        NumberSequenceId id = new NumberSequenceId();
        id.setNamespaceId(namespaceId);
        s.setId(id);
        s.setCurrentSeq(0);
        s.setPrefix(prefix);
        sequenceRepository.save(s);
        return build(s);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addHistory(UUID namespaceId, String number, NamespaceContext context) {
        NumberHistoryTb h = new NumberHistoryTb();
        NumberHistoryId id = new NumberHistoryId();
        id.setNamespaceId(namespaceId);
        h.setId(id);
        h.setNumber(number);
        h.setCreatedBy(context.getNamespaceUserId());
        h.setCreatedDate(LocalDateTime.now());
        historyRepository.save(h);
    }

    public NumberRecord addNumber(String type, UUID namespaceId, String name, String prefix, int length, String suffix,
                                  NamespaceContext context) {
        NumberTb n = new NumberTb();
        NumberId id = new NumberId();
        id.setNamespaceId(namespaceId);
        n.setId(id);
        n.setType(type);
        n.setName(name);
        n.setPrefix(prefix);
        n.setLength(length);
        n.setSuffix(suffix);
        n.setCreatedBy(context.getNamespaceUserId());
        n.setCreatedDate(LocalDateTime.now());
        numberRepository.save(n);
        return build(n);
    }
}
