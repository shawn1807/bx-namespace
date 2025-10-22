package com.tsu.namespace.service.impl;

import com.tsu.namespace.api.AuthLogin;
import com.tsu.base.api.UserBase;
import com.tsu.namespace.api.UserProfile;
import com.tsu.namespace.api.namespace.DomainObjectBuilder;
import com.tsu.base.enums.BaseParamName;
import com.tsu.namespace.helper.UserDbHelper;
import com.tsu.namespace.record.UserRecord;
import com.tsu.namespace.service.UserService;
import com.tsu.base.val.UserVal;
import com.tsu.common.api.BasePrincipal;
import com.tsu.common.utils.ParamValidator;
import com.tsu.common.vo.Email;
import com.tsu.common.vo.Text;
import com.tsu.security.AppSecurityContext;
import com.tsu.security.AppSecurityContextInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Transactional
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDbHelper userDbHelper;

    @Autowired
    private DomainObjectBuilder builder;
    @Autowired
    private AppSecurityContextInitializer initializer;


    public UserVal register(Text name, Email email, String imageUrl, UserProfile profile, AuthLogin login) {
        log.info("Registering new user with email: {} and name: {}", email, name);
        log.debug("User profile - firstName: {}, lastName: {}, hasLogin: {}",
                profile != null ? profile.getFirstName() : null,
                profile != null ? profile.getLastName() : null,
                login != null);

        ParamValidator.builder() //
                .withNonNullOrEmpty(name, BaseParamName.NAME)//
                .withVerifyEmail(email)//
                .throwIfErrors();

        log.trace("Creating user record in database");
        UserRecord userRecord = userDbHelper.register(name.strip(), email.toString(), null, imageUrl,
                profile.getFirstName(), profile.getLastName(), profile, null);
        log.debug("User record created with ID: {}", userRecord.id());

        if (login != null) {
            log.debug("Adding login authentication for user: {}", userRecord.id());
            userDbHelper.addLogin(userRecord.id(), login, null, userRecord.id());
            log.info("Login authentication added for user: {}", userRecord.id());
        }

        log.info("Successfully registered user: {} with email: {}", userRecord.id(), email);
        return userRecord.getValue();
    }

    @Override
    public UserBase addUser(Text name, Email email, String imageUrl, UserProfile profile) {
        log.info("Adding new user with email: {} and name: {}", email, name);
        log.debug("User profile details - firstName: {}, lastName: {}",
                profile != null ? profile.getFirstName() : null,
                profile != null ? profile.getLastName() : null);

        ParamValidator.builder() //
                .withNonNullOrEmpty(name, BaseParamName.NAME)//
                .withVerifyEmail(email)//
                .throwIfErrors();

        AppSecurityContext context = initializer.initializeAndVerify();
        log.trace("Security context initialized by principal: {}", context.getPrincipal().id());

        log.debug("Creating user with principal: {} as creator", context.getPrincipal().id());
        UserRecord userRecord = userDbHelper.addUser(name.strip(), email.toString(), imageUrl, profile,
                context.getPrincipal().id());

        UserBase user = builder.build(userRecord, context);
        log.info("Successfully added user with email: {}", email);

        return user;
    }


    @Override
    public Optional<UserBase> findUser(BasePrincipal user) {
        log.debug("Searching for user by principal ID: {}", user.id());

        ParamValidator.builder()
                .withNonNull(user, BaseParamName.USER)
                .throwIfErrors();
        AppSecurityContext context = initializer.initializeAndVerify();
        log.trace("Security context initialized for principal search by: {}", context.getPrincipal().id());
        Optional<UserBase> foundUser = userDbHelper.findUserById(user.id())
                .map(userRecord -> {
                    log.debug("User found for principal ID: {}", user.id());
                    return builder.build(userRecord, context);
                });

        if (foundUser.isEmpty()) {
            log.warn("No user found for principal ID: {}", user.id());
        } else {
            log.info("Successfully found user for principal ID: {}", user.id());
        }

        return foundUser;
    }

    @Override
    public Optional<UserVal> findUserByEmail(Text email) {
        return userDbHelper.findUserByEmail(email.strip())
                .map(UserRecord::getValue);
    }
}
