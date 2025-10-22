package com.tsu.namespace.repo;

import com.tsu.namespace.entities.EventAuditTb;
import com.tsu.namespace.entities.id.EventAuditId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventAuditRepository extends JpaRepository<EventAuditTb, EventAuditId> {



}
