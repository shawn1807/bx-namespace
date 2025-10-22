package com.tsu.namespace.api.namespace;

import com.tsu.namespace.api.NamespaceUsers;
import com.tsu.namespace.val.NspUsrVal;
import com.tsu.common.data.Pair;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CachedNamespaceUsers implements NamespaceUsers {

    private final Map<UUID, NspUsrVal> byUUID;
    private final Map<Integer, NspUsrVal> byId;

    public CachedNamespaceUsers(List<NspUsrVal> users) {
        this.byUUID = users.stream()
                .collect(Collectors.toMap(NspUsrVal::principalId, u -> u));
        this.byId = users
                .stream()
                .collect(Collectors.toMap(NspUsrVal::userId, u -> u));
    }

    @Override
    public Optional<NspUsrVal> find(Integer userId) {
        return Optional.ofNullable(byId.get(userId));
    }

    @Override
    public Optional<NspUsrVal> find(UUID principalId) {
        return Optional.ofNullable(byUUID.get(principalId));
    }

    @Override
    public Map<Integer, NspUsrVal> getUserMap() {
        return Collections.unmodifiableMap(this.byId);
    }
}
