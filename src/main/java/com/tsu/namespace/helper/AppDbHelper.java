package com.tsu.namespace.helper;

import com.tsu.common.api.ActionPack;
import com.tsu.common.jpa.JsonValueUtils;
import com.tsu.auth.security.NamespaceContext;
import com.tsu.namespace.entities.EventAuditTb;
import com.tsu.namespace.entities.id.EventAuditId;
import com.tsu.namespace.repo.EventAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AppDbHelper {

    private final EventAuditRepository auditRepository;

    public void audit(UUID namespaceId, UUID entryId, ActionPack pack, NamespaceContext context) {
        EventAuditTb audit = new EventAuditTb();
        EventAuditId id = new EventAuditId();
        id.setNamespaceId(namespaceId);
        audit.setId(id);
        audit.setAction(pack.action().getName());
        audit.setParams(JsonValueUtils.getInstance().encodeAsJson(pack.params()));
        audit.setTxid(context.getSecurityContext().getTxid());
        audit.setEntryId(entryId);
        audit.setCreatedBy(context.getNamespaceUserId());
        audit.setCreatedDate(LocalDateTime.now());
        auditRepository.save(audit);
    }
}
