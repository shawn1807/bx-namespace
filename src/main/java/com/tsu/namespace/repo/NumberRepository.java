package com.tsu.namespace.repo;

import com.tsu.namespace.entities.NumberTb;
import com.tsu.namespace.entities.id.NumberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface NumberRepository extends JpaRepository<NumberTb, NumberId> {


    Stream<NumberTb> findByIdNamespaceId(UUID namespaceId);

    Stream<NumberTb> findByIdNamespaceIdAndType(UUID namespaceId, String type);

    Optional<NumberTb> findByIdNamespaceIdAndName(UUID namespaceId, String name);


}
