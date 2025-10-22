package com.tsu.namespace.api.namespace;

import com.tsu.base.api.NamespaceUsers;
import com.tsu.namespace.helper.NamespaceDbHelper;
import com.tsu.base.val.NspUsrVal;
import com.tsu.common.data.Pair;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CachedNamespaceUsers implements NamespaceUsers {

    private final Map<Pair<UUID,Integer>, NspUsrVal> users;

    public CachedNamespaceUsers(Stream<NspUsrVal> users) {
        this.users = users
                .collect(Collectors.toMap(u-> Pair.of(u.principalId(),u.userId()), u -> u));
    }

    @Override
    public Optional<NspUsrVal> find(Integer userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<NspUsrVal> find(UUID principalId) {
        return Optional.empty();
    }

    @Override
    public Map<Integer, NspUsrVal> getUserMap() {
        return Collections.unmodifiableMap(users);
    }
}
