package com.tsu.namespace.repo;

import com.tsu.namespace.entities.SubscriptionTb;
import com.tsu.namespace.entities.id.SubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionTb, SubscriptionId> {

    Optional<SubscriptionTb> findByIdNamespaceId(UUID namespaceId);
}
