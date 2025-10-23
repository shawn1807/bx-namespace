package com.tsu.namespace.entities.id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Composite ID for PlaceAltName entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceAltNameId implements Serializable {
    private UUID namespaceId;
    private Long id;
}
