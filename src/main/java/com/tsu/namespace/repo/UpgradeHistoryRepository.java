package com.tsu.namespace.repo;

import com.tsu.namespace.entities.UpgradeHistoryTb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UpgradeHistoryRepository extends JpaRepository<UpgradeHistoryTb, Integer> {


}
