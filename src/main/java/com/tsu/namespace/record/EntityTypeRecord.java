package com.tsu.namespace.record;

import com.tsu.namespace.entities.EntityTypeTb;
import com.tsu.base.val.EntityTypeVal;
import com.tsu.common.api.BasePrincipal;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Consumer;


@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class EntityTypeRecord {

    @ToString.Include
    private final EntityTypeTb tb;
    private final Consumer<EntityTypeTb> persist;

    public void persist() {
        persist.accept(tb);
    }


    public EntityTypeVal getValue() {
        return new EntityTypeVal(tb.getId(), tb.getName(), tb.getCreatedBy(),
                tb.getCreatedDate());
    }


    public String getName() {
        return tb.getName();
    }

    public Integer getId() {
        return tb.getId();
    }
}
