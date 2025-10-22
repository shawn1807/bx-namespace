package com.tsu.namespace.repo;

import com.tsu.namespace.entities.ResourceWeeklyWindowTb;
import com.tsu.namespace.entities.id.ResourceWeeklyWindowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface ResourceWeeklyWindowRepository extends JpaRepository<ResourceWeeklyWindowTb, ResourceWeeklyWindowId> {

    /**
     * Find all weekly windows for a specific resource
     */
    @Query(value = """
            SELECT rww.*
            FROM resource_weekly_window rww
            WHERE rww.namespace_id = ?1 AND rww.resource_id = ?2
            ORDER BY rww.dow, rww.start_local
            """, nativeQuery = true)
    Stream<ResourceWeeklyWindowTb> findByNamespaceIdAndResourceIdOrderByDowAndStartLocal(UUID namespaceId, UUID resourceId);

    /**
     * Find weekly windows for a specific day of week
     */
    @Query(value = """
            SELECT rww.*
            FROM resource_weekly_window rww
            WHERE rww.namespace_id = ?1 AND rww.resource_id = ?2 AND rww.dow = ?3
            ORDER BY rww.start_local
            """, nativeQuery = true)
    Stream<ResourceWeeklyWindowTb> findByNamespaceIdAndResourceIdAndDow(UUID namespaceId, UUID resourceId, Integer dow);
}
