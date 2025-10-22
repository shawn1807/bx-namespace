package com.tsu.namespace.api.manager;

import com.tsu.enums.BaseExceptionCode;
import com.tsu.enums.BaseParamName;
import com.tsu.namespace.api.NumberManager;
import com.tsu.namespace.api.NumberSeq;
import com.tsu.namespace.api.namespace.NamespaceObjectFactory;
import com.tsu.auth.permissions.NamespaceAction;
import com.tsu.namespace.helper.NumberDbHelper;
import com.tsu.namespace.record.NumberRecord;
import com.tsu.namespace.record.NumberSeqRecord;
import com.tsu.workspace.request.AddNumber;
import com.tsu.namespace.val.NumberSeqVal;
import com.tsu.common.api.ActionPack;
import com.tsu.common.exception.UserException;
import com.tsu.common.utils.ParamValidator;
import com.tsu.auth.security.NamespaceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class NamespaceNumberManager implements NumberManager {

    private final NamespaceContext context;
    private final NumberDbHelper dbHelper;
    private final NamespaceObjectFactory factory;

    public NamespaceNumberManager(NamespaceContext context, NumberDbHelper dbHelper, NamespaceObjectFactory factory) {
        this.context = context;
        this.dbHelper = dbHelper;
        this.factory = factory;
    }

    @Override
    public Optional<NumberSeq> findNumberSeq(String name) {
        return dbHelper.findNumberByNamespaceIdAndName(context.getNamespaceId(), name)
                .map(record -> factory.build(record, context));
    }

    @Override
    public Stream<NumberSeqVal> getNumbers() {
        return dbHelper.findNumberByNamespaceId(context.getNamespaceId())
                .map(NumberRecord::getValue);

    }

    @Override
    public Stream<NumberSeq> findNumbers(String type) {
        return dbHelper.findNumberByNamespaceIdAndType(context.getNamespaceId(), type)
                .map(numberSequenceTb -> factory.build(numberSequenceTb, context));
    }

    @Override
    public NumberSeq addNumber(AddNumber request) {
        ParamValidator.builder()
                .withNonNullOrEmpty(request.getName(), BaseParamName.NAME)
                .withNonNullOrEmpty(request.getType(), BaseParamName.TYPE)
                .throwIfErrors();
        dbHelper.findNumberByNamespaceIdAndName(context.getNamespaceId(), request.getName())//
                .ifPresent((number) -> {
                    throw new UserException(BaseExceptionCode.NAME_EXISTS, Map.of(BaseParamName.NAME, request.getName()));
                });
        context.getNamespace().getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.CREATE_NUMBER, request));
        NumberRecord record = dbHelper.addNumber(request.getType().strip(), context.getNamespaceId(), request.getName().strip(), request.getPrefix().strip(),
                request.getLength(), request.getSuffix(), context);
        return factory.build(record, context);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public String next(String prefix) {
        NumberSeqRecord numberSeq = dbHelper.findNumberSeqByNamespaceIdAndPrefix(context.getNamespaceId(), prefix)
                .orElseGet(() ->
                        dbHelper.addNumberSequence(context.getNamespaceId(), prefix));
        int next = numberSeq.increment();
        return Optional.ofNullable(prefix).orElse("") + next;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public String next(String prefix, String suffix) {
        return next(prefix) + Optional.ofNullable(suffix).orElse("");
    }

}
