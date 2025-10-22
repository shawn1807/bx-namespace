package com.tsu.namespace.repo;

import com.tsu.namespace.entities.NumberSequenceTb;
import com.tsu.namespace.entities.id.NumberSequenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface NumberSequenceRepository extends JpaRepository<NumberSequenceTb, NumberSequenceId> {

    Stream<NumberSequenceTb> findByIdNamespaceId(UUID namespaceId);

    Optional<NumberSequenceTb> findByIdNamespaceIdAndPrefix(UUID namespaceId, String prefix);
}
