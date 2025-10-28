package com.tsu.namespace.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsu.auth.api.BasePrincipal;
import com.tsu.auth.security.AppJwtAuthenticationToken;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.auth.security.AppSecurityContextInitializer;
import com.tsu.common.utils.ParamValidator;
import com.tsu.common.val.UserVal;
import com.tsu.common.vo.Email;
import com.tsu.common.vo.Text;
import com.tsu.enums.BaseParamName;
import com.tsu.namespace.api.RegisterUser;
import com.tsu.namespace.api.UpdateUser;
import com.tsu.namespace.api.UserBase;
import com.tsu.namespace.api.UserProfile;
import com.tsu.namespace.api.namespace.NamespaceObjectFactory;
import com.tsu.namespace.dto.LoginUserInfoDto;
import com.tsu.namespace.dto.NamespaceDto;
import com.tsu.namespace.helper.UserDbHelper;
import com.tsu.namespace.record.UserRecord;
import com.tsu.namespace.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Transactional
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDbHelper userDbHelper;

    @Autowired
    private NamespaceObjectFactory factory;
    @Autowired
    private AppSecurityContextInitializer initializer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserVal register(RegisterUser register) {
        log.info("Registering a new user: {} ", register);
        ParamValidator.builder() //
                .withNonNullOrEmpty(register.displayName(), BaseParamName.NAME)//
                .withVerifyEmail(Email.of(register.email()))//
                .throwIfErrors();
        log.trace("Creating user record in database");
        UserRecord userRecord = userDbHelper.register(register.displayName(), register.email(), register.phone(), register.imageURL(),
                register.firstName(), register.lastName(), register.preferences());
        userRecord.setLanguageTag(register.languageTag());
        userRecord.setTimezoneId(register.timezoneId());
        userRecord.setDatePattern(register.datePattern());
        userRecord.setDatetimePattern(register.datetimePattern());
        log.debug("User record created with ID: {}", userRecord.id());
        if (register.login() != null) {
            log.debug("Adding login authentication for user: {}", userRecord.id());
            userDbHelper.addLogin(userRecord.id(), register.login(), null, userRecord.id());
            log.info("Login authentication added for user: {}", userRecord.id());
        }
        log.info("Successfully registered user: {} with email: {}", userRecord.id(), register.email());
        userRecord.persist();
        return userRecord.getValue();
    }

    @Override
    public UserBase addUser(Text name, Email email, Text phone) {
        log.info("upsert new user: {}", email);
        ParamValidator.builder() //
                .withNonNullOrEmpty(name, BaseParamName.NAME)//
                .withVerifyEmail(email)//
                .throwIfErrors();
        UserRecord userRecord = userDbHelper.addUser(name.strip(), email.toString(), phone != null ? phone.strip() : null);
        AppSecurityContext context = initializer.initializeAndVerify();
        return factory.build(userRecord, context);
    }

    @Override
    public UserProfile updateContextUser(UpdateUser update) {
        log.info("update context user: {}", update);
        ParamValidator.builder() //
                .withNonNullOrEmpty(update.displayName(), BaseParamName.NAME)//
                .throwIfErrors();
        AppSecurityContext context = initializer.initializeAndVerify();
        UserBase userBase = context.getUser()
                .orElseThrow(() -> new IllegalStateException("Unable to update context user profile"));
        userBase.setDisplayName(Text.of(update.displayName()));
        userBase.setImageUrl(update.imageURL());
        userBase.setName(Text.of(update.firstName()), Text.of(update.lastName()));
        userBase.setPreferences(update.preferences());
        userBase.setPhone(Text.of(update.phone()));
        Optional.ofNullable(update.languageTag())
                .ifPresent(tag -> {
                    try {
                        userBase.setLocale(Locale.forLanguageTag(tag));
                    } catch (Exception e) {
                        log.error("error set language tag: {} ", tag);
                        userBase.setLocale(Locale.getDefault());
                    }
                });
        Optional.ofNullable(update.timezoneId())
                .ifPresent(zoneId -> {
                    try {
                        userBase.setZone(ZoneId.of(zoneId));
                    } catch (Exception e) {
                        log.error("error set zoneId: {} ", zoneId);
                        userBase.setZone(ZoneId.systemDefault());
                    }
                });
        userBase.setDateFormat(update.datePattern());
        userBase.setDateTimeFormat(update.datetimePattern());
        return userBase.toProfile();
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
                    return factory.build(userRecord, context);
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

    @Override
    public LoginUserInfoDto getContextUserInfo() {
        log.debug("Getting user profile information");
        boolean userRegistered = initializer.isUserRegistered();
        log.debug("User registration status: {}", userRegistered);
        String displayName = null;
        String firstName = null;
        String lastName = null;
        String email = null;
        String imageURL = null;
        List<NamespaceDto> namespaceDtoList = Collections.emptyList();
        if (!userRegistered) {
            log.info("User not registered, processing authentication token for registration");
            SecurityContext springContext = SecurityContextHolder.getContext();
            Authentication authentication = springContext.getAuthentication();
            if (authentication instanceof AppJwtAuthenticationToken token) {
                displayName = token.getDisplayName().orElse(null);
                firstName = token.getFirstName().orElse(null);
                lastName = token.getLastName().orElse(null);
                email = token.getEmail().orElse(null);
                log.debug("Extracted user info from token - email: {}, username: {}", email, token.getUsername());
            }
        } else {
            log.debug("User already registered, retrieving profile from database");
            AppSecurityContext context = initializer.initializeAndVerify();
            UserBase userBase = findUser(context.getPrincipal())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            UserVal user = userBase.getValue();
            displayName = user.displayName();
            email = user.email();
            imageURL = user.imageUrl();
            firstName = user.firstName();
            lastName = user.lastName();
            log.debug("Retrieved user profile - email: {}, displayName: {}", email, displayName);
            namespaceDtoList = buildNamespaceDtoList(userBase);
            log.debug("Found {} namespaces for user", namespaceDtoList.size());
        }
        LoginUserInfoDto userInfoDto = LoginUserInfoDto.builder()
                .displayName(displayName)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .imageURL(imageURL)
                .registered(userRegistered)
                .namespaces(namespaceDtoList)
                .build();
        log.info("Returning user profile Dto for email: {}, registered: {}", email, userRegistered);
        return userInfoDto;
    }


    private List<NamespaceDto> buildNamespaceDtoList(UserBase userBase) {
        return userBase.findNamespaces()
                .map(n -> NamespaceDto.builder()
                        .id(n.id().toString())
                        .name(n.name())
                        .status(n.active() ? "active" : "inactive")
                        .build())
                .toList();
    }
}
