package com.tsu.namespace.repo;

import com.tsu.namespace.entities.SubscriptionPlanTb;
import com.tsu.namespace.entities.id.SubscriptionPlanId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlanTb, SubscriptionPlanId> {

    Optional<SubscriptionPlanTb> findByName(String name);

}
