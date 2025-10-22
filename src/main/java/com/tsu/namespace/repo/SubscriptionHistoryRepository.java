package com.tsu.namespace.repo;

import com.tsu.namespace.entities.SubscriptionHistoryTb;
import com.tsu.namespace.entities.id.SubscriptionHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistoryTb, SubscriptionHistoryId> {

    Stream<SubscriptionHistoryTb> findByIdNamespaceIdOrderByCreatedDateAsc(UUID namespaceId);

}
