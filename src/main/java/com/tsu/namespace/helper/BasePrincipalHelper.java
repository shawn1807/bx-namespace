package com.tsu.namespace.helper;

import com.tsu.base.api.PrincipalType;
import com.tsu.namespace.repo.BasePrincipalRepository;
import com.tsu.base.val.BasePrincipalVal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class BasePrincipalHelper {

    private final BasePrincipalRepository repository;
    private final Map<UUID, Optional<BasePrincipalVal>> principalMap = new Hashtable<>();


    public Optional<BasePrincipalVal> findPrincipal(UUID id) {
        return principalMap.computeIfAbsent(id, principal -> repository.findById(id)
                .map(p -> new BasePrincipalVal(p.getId(), p.getName(), p.getType())));
    }


}
