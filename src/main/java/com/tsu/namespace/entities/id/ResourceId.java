package com.tsu.namespace.entities.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ResourceId implements Serializable {

    @Column(name = "namespace_id", nullable = false)
    private UUID namespaceId;

    @Column(name = "id", nullable = false)
    private UUID id;
}
