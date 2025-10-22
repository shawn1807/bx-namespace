package com.tsu.namespace.helper;

import com.tsu.namespace.api.AuthLogin;
import com.tsu.base.api.PrincipalType;
import com.tsu.namespace.entities.BasePrincipalTb;
import com.tsu.namespace.entities.LoginTb;
import com.tsu.namespace.entities.UserBaseTb;
import com.tsu.base.enums.AuthProvider;
import com.tsu.namespace.record.LoginRecord;
import com.tsu.namespace.record.UserRecord;
import com.tsu.namespace.repo.BasePrincipalRepository;
import com.tsu.namespace.repo.LoginRepository;
import com.tsu.namespace.repo.UserBaseRepository;
import com.tsu.base.service.IDGeneratorService;
import com.tsu.common.api.BasePrincipal;
import com.tsu.common.jpa.JsonValueUtils;
import com.tsu.common.utils.CompositeValueObserver;
import com.tsu.common.utils.LazyCacheLoader;
import com.tsu.security.AppSecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDbHelper {

    private final IDGeneratorService idGeneratorService;
    private final UserBaseRepository userRepository;
    private final LoginRepository loginRepository;
    private final BasePrincipalRepository basePrincipalRepository;

    public BasePrincipal createBasePrincipal(String name, PrincipalType type) {
        BasePrincipalTb base = new BasePrincipalTb();
        UUID id = idGeneratorService.nextUUID();
        base.setId(id);
        base.setName(name);
        base.setType(type);
        basePrincipalRepository.save(base);
        return BasePrincipal.of(id);

    }

    public UserRecord register(String displayName, String email, String phone, String imageUrl, String firstName, String lastName, Object profile, Object preference) {
        BasePrincipal base = createBasePrincipal(displayName, PrincipalType.USER);
        UserBaseTb e = new UserBaseTb();
        e.setId(base.id());
        e.setDisplayName(displayName);
        e.setActive(true);
        e.setPhone(phone);
        e.setEmail(email);
        e.setFirstName(firstName);
        e.setLastName(lastName);
        e.setImageUrl(imageUrl);
        e.setProfile(JsonValueUtils.getInstance().encodeAsJson(profile));
        e.setPreference(JsonValueUtils.getInstance().encodeAsJson(preference));
        e.setCreatedDate(LocalDateTime.now());
        e.setCreatedBy(base.id());
        e.setModifiedBy(base.id());
        e.setModifiedDate(LocalDateTime.now());
        userRepository.save(e);
        return buildUserRecord(e);
    }

    public UserRecord addUser(String name, String email, String phone, Object profile, UUID createdBy) {
        return addUser(name, email, phone, null, profile, null, createdBy);
    }

    public UserRecord addUser(String name, String email, String phone, String imageUrl, Object profile, Object preference, UUID createdBy) {
        BasePrincipal base = createBasePrincipal(name, PrincipalType.USER);
        UserBaseTb e = new UserBaseTb();
        e.setId(base.id());
        e.setDisplayName(name);
        e.setActive(true);
        e.setPhone(phone);
        e.setEmail(email);
        e.setImageUrl(imageUrl);
        e.setProfile(JsonValueUtils.getInstance().encodeAsJson(profile));
        e.setPreference(JsonValueUtils.getInstance().encodeAsJson(preference));
        e.setCreatedDate(LocalDateTime.now());
        e.setCreatedBy(createdBy);
        e.setModifiedBy(createdBy);
        e.setModifiedDate(LocalDateTime.now());
        userRepository.save(e);
        return buildUserRecord(e);
    }

    private UserRecord buildUserRecord(UserBaseTb u) {
        LazyCacheLoader<Optional<BasePrincipalTb>> basePrincipal = LazyCacheLoader.of(() -> basePrincipalRepository.findById(u.getId()));
        CompositeValueObserver observer = new CompositeValueObserver();
        return new UserRecord(u, tb -> {
            userRepository.save(tb);
            if (observer.isUpdated()) {
                basePrincipal.get()
                        .ifPresent(bp -> {
                            bp.setName(tb.getDisplayName());
                            basePrincipalRepository.save(bp);
                        });
            }
        }, observer.createValueObserver(value -> log.debug("user name updated to: {}", value)));
    }

    public LoginRecord addLogin(UUID userId, AuthLogin login, Object props, UUID createdBy) {
        LoginTb tb = new LoginTb();
        tb.setUserId(userId);
        tb.setAuthId(login.id());
        tb.setProps(JsonValueUtils.getInstance().encodeAsJson(props));
        tb.setProvider(login.authProvider());
        tb.setCreatedBy(createdBy);
        tb.setCreatedDate(LocalDateTime.now());
        tb.setActive(true);
        loginRepository.save(tb);
        return new LoginRecord(tb, loginRepository::save);
    }

    public Optional<LoginRecord> findLogins(AuthProvider provider, String authId) {
        return loginRepository.findByProviderAndAuthId(provider, authId)
                .map(tb -> new LoginRecord(tb, loginRepository::save));
    }



    public Optional<UserRecord> findUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::buildUserRecord);
    }

    public Optional<UserRecord> findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::buildUserRecord);
    }

}
