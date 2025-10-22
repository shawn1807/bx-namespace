package com.tsu.namespace.repo;

import com.tsu.base.api.NamespaceUserType;
import com.tsu.namespace.entities.NamespaceUserTb;
import com.tsu.namespace.entities.id.NamespaceUserId;
import com.tsu.base.val.NspUsrVal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface NamespaceUserRepository extends JpaRepository<NamespaceUserTb, NamespaceUserId> {

    @Query(value = """
            SELECT u.id as principalId, nu.id as userId, nu.active, nu.display_name, u.image_url, r.name as role
                FROM USER_BASE u JOIN NAMESPACE_USER nu ON u.id = nu.principal_id
                LEFT JOIN namespace_role r on nu.role_id = r.id AND nu.role_namespace_id = r.namespace_id
                WHERE nu.namespace_id = ?1
                ORDER BY nu.id
            """, nativeQuery = true)
    Stream<NspUsrVal> findNamespaceJoinedUserInfoByNamespaceId(UUID namespaceId);

    Optional<NamespaceUserTb> findByIdNamespaceIdAndIdId(UUID namespaceId,Integer id);

    Stream<NamespaceUserTb> findByIdNamespaceId(UUID namespaceId);

    Optional<NamespaceUserTb> findByIdNamespaceIdAndPrincipalId(UUID namespaceId,UUID principalId);

    Stream<NamespaceUserTb> findByIdNamespaceIdAndType(UUID namespaceId, NamespaceUserType type);
}
