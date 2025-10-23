package com.tsu.namespace.entities.id;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Composite ID for Place entity (namespace_id + id).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceId implements Serializable {
    @Column(name = "namespace_id", nullable = false)
    private UUID namespaceId;

    @Column(name = "id", nullable = false)
    private UUID id;
}
