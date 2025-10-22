package com.tsu.namespace.api.entity;

import com.tsu.base.api.EntityType;
import com.tsu.base.api.Namespace;
import com.tsu.namespace.record.EntityTypeRecord;
import com.tsu.base.val.EntityTypeVal;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class EntityTypeImpl implements EntityType {

    @ToString.Include
    private final EntityTypeRecord record;
    private final Namespace namespace;


    @Override
    public String getName() {
        return record.getName();
    }


    @Override
    public EntityTypeVal getValue() {
        return record.getValue();
    }

    @Override
    public Namespace getNamespace() {
        return namespace;
    }


}
