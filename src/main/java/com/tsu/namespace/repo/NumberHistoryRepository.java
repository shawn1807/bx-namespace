package com.tsu.namespace.repo;

import com.tsu.namespace.entities.NumberHistoryTb;
import com.tsu.namespace.entities.id.NumberHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NumberHistoryRepository extends JpaRepository<NumberHistoryTb, NumberHistoryId> {


}
