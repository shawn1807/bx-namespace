package com.tsu.namespace.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsu.auth.api.BasePrincipal;
import com.tsu.auth.security.AppJwtAuthenticationToken;
import com.tsu.common.utils.ParamValidator;
import com.tsu.common.vo.Email;
import com.tsu.common.vo.Text;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.auth.security.AppSecurityContextInitializer;
import com.tsu.auth.api.AuthLogin;
import com.tsu.enums.BaseParamName;
import com.tsu.namespace.api.UserBase;
import com.tsu.namespace.api.UserProfile;
import com.tsu.namespace.api.namespace.NamespaceObjectFactory;
import com.tsu.namespace.dto.FullUserProfileDto;
import com.tsu.namespace.dto.NamespaceDto;
import com.tsu.namespace.dto.UserProfileDto;
import com.tsu.namespace.helper.UserDbHelper;
import com.tsu.namespace.record.UserRecord;
import com.tsu.namespace.request.UpdateFullUserProfileRequest;
import com.tsu.namespace.request.UpdateUserProfileRequest;
import com.tsu.namespace.request.UserProfileSettingsDto;
import com.tsu.namespace.service.UserService;
import com.tsu.common.val.UserVal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
        String firstName = null;
        String lastName = null;
        if (profile != null) {
            firstName = profile.getFirstName();
            lastName = profile.getLastName();
        }
        log.trace("Creating user record in database");
        UserRecord userRecord = userDbHelper.register(name.strip(), email.toString(), null, imageUrl,
                firstName, lastName, profile, null);
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

        UserBase user = factory.build(userRecord, context);
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
    public UserProfileDto getUserProfile() {
        log.debug("Getting user profile information");
        boolean userRegistered = initializer.isUserRegistered();
        log.debug("User registration status: {}", userRegistered);

        String userName = null;
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
                userName = token.getUsername().orElse(null);
                firstName = token.getFirstName().orElse(null);
                lastName = token.getLastName().orElse(null);
                email = token.getEmail().orElse(null);
                log.debug("Extracted user info from token - email: {}, username: {}", email, userName);
            }
            UserProfile profile = new UserProfile();
            profile.setFirstName(firstName);
            profile.setLastName(lastName);
            log.debug("Registering user with extracted profile information");
//            userService.register(Text.of(displayName), Email.of(email), null, profile,
//                    new AuthLogin(AuthProvider.KEYCLOAK, authentication.getName()));
            log.info("User registration completed for email: {}", email);
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

        UserProfileDto Dto = UserProfileDto.builder()
                .displayName(displayName)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .imageURL(imageURL)
                .username(userName)
                .registered(userRegistered)
                .namespaces(namespaceDtoList)
                .build();
        log.info("Returning user profile Dto for email: {}, registered: {}", email, userRegistered);
        return Dto;
    }

    @Override
    public UserProfileDto updateUserProfile(UpdateUserProfileRequest request) {
        log.info("Updating user profile");
        log.debug("Update request - displayName: {}, email: {}, firstName: {}, lastName: {}, imageURL: {}",
                request.getDisplayName(), request.getEmail(), request.getFirstName(),
                request.getLastName(), request.getImageURL());

        AppSecurityContext context = initializer.initializeAndVerify();
        UserBase userBase = findUser(context.getPrincipal())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Update user profile fields
        if (request.getDisplayName() != null) {
            log.debug("Updating displayName to: {}", request.getDisplayName());
            userBase.setDisplayName(Text.of(request.getDisplayName()));
        }

        if (request.getEmail() != null) {
            log.debug("Updating email to: {}", request.getEmail());
            userBase.setEmail(Email.of(request.getEmail()));
        }

        if (request.getFirstName() != null || request.getLastName() != null) {
            String firstName = request.getFirstName() != null ? request.getFirstName() : userBase.getValue().firstName();
            String lastName = request.getLastName() != null ? request.getLastName() : userBase.getValue().lastName();
            log.debug("Updating name to: {} {}", firstName, lastName);
            userBase.setName(Text.of(firstName), Text.of(lastName));
        }

        if (request.getImageURL() != null) {
            log.debug("Updating imageURL to: {}", request.getImageURL());
            userBase.setImageUrl(request.getImageURL());
        }

        // Retrieve updated user information
        UserVal user = userBase.getValue();
        List<NamespaceDto> namespaceDtoList = buildNamespaceDtoList(userBase);

        UserProfileDto Dto = UserProfileDto.builder()
                .displayName(user.displayName())
                .email(user.email())
                .firstName(user.firstName())
                .lastName(user.lastName())
                .imageURL(user.imageUrl())
                .registered(true)
                .namespaces(namespaceDtoList)
                .build();

        log.info("User profile updated successfully for email: {}", user.email());
        return Dto;
    }

    @Override
    public UserProfileSettingsDto getUserSettings() {
        log.info("Getting user profile settings");

        AppSecurityContext context = initializer.initializeAndVerify();
        UserBase userBase = findUser(context.getPrincipal())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Map<String, String> preferences = userBase.getPreference();
        if (preferences == null || preferences.isEmpty()) {
            log.debug("No preferences found, returning default settings");
            return UserProfileSettingsDto.builder().build();
        }

        try {
            // Try to get the settings object from preferences
            String settingsJson = preferences.get("settings");
            if (settingsJson != null) {
                UserProfileSettingsDto settings = objectMapper.readValue(settingsJson, UserProfileSettingsDto.class);
                log.info("User settings retrieved successfully");
                return settings;
            } else {
                log.debug("No settings found in preferences, returning defaults");
                return UserProfileSettingsDto.builder().build();
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing user settings from preferences", e);
            return UserProfileSettingsDto.builder().build();
        }
    }

    @Override
    public UserProfileSettingsDto updateUserSettings(UserProfileSettingsDto settings) {
        log.info("Updating user profile settings");

        AppSecurityContext context = initializer.initializeAndVerify();
        UserBase userBase = findUser(context.getPrincipal())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        try {
            // Convert settings to JSON and store in preferences
            String settingsJson = objectMapper.writeValueAsString(settings);
            Map<String, String> preferences = userBase.getPreference();
            if (preferences == null) {
                preferences = new HashMap<>();
            } else {
                preferences = new HashMap<>(preferences); // Make mutable copy
            }
            preferences.put("settings", settingsJson);
            userBase.setPreferences(preferences);

            log.info("User settings updated successfully");
            return settings;
        } catch (JsonProcessingException e) {
            log.error("Error serializing user settings to JSON", e);
            throw new IllegalStateException("Failed to update user settings", e);
        }
    }

    @Override
    public FullUserProfileDto getFullProfile() {
        log.info("Getting full user profile with all available information");
        AppSecurityContext context = initializer.initializeAndVerify();
        UserBase userBase = findUser(context.getPrincipal())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Get user value
        UserVal user = userBase.getValue();
        log.debug("Retrieved user value for: {}", user.email());

        // Get preferences
        Map<String, String> preferences = userBase.getPreference();

        // Get region from profile
        String region = null;
        UserProfile profile = userBase.getProfile(UserProfile.class).orElse(UserProfile.builder().build());
        // Build full Dto
        FullUserProfileDto Dto = FullUserProfileDto.builder()
                .id(user.id().toString())
                .displayName(user.displayName())
                .firstName(user.firstName())
                .lastName(user.lastName())
                .email(user.email())
                .imageURL(user.imageUrl())
                .phone(user.phone())
                .region(region)
                .dateFormat(profile.getDateFormat())
                .dateTimeFormat(profile.getDateTimeFormat())

                .preferences(preferences)
                .build();

        log.info("Full user profile retrieved successfully for user: {}", user.email());
        return Dto;
    }

    @Override
    public FullUserProfileDto updateFullProfile(UpdateFullUserProfileRequest request) {
        log.info("Updating full user profile");
        log.debug("Update request - displayName: {}, email: {}, firstName: {}, lastName: {}, imageURL: {}, phone: {}, region: {}",
                request.getDisplayName(), request.getEmail(), request.getFirstName(),
                request.getLastName(), request.getImageURL(), request.getPhone(), request.getRegion());

        AppSecurityContext context = initializer.initializeAndVerify();
        UserBase userBase = findUser(context.getPrincipal())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Update display name
        if (request.getDisplayName() != null) {
            log.debug("Updating displayName to: {}", request.getDisplayName());
            userBase.setDisplayName(Text.of(request.getDisplayName()));
        }

        // Update email
        if (request.getEmail() != null) {
            log.debug("Updating email to: {}", request.getEmail());
            userBase.setEmail(Email.of(request.getEmail()));
        }

        // Update name (firstName and lastName)
        if (request.getFirstName() != null || request.getLastName() != null) {
            UserVal currentUser = userBase.getValue();
            String firstName = request.getFirstName() != null ? request.getFirstName() : currentUser.firstName();
            String lastName = request.getLastName() != null ? request.getLastName() : currentUser.lastName();
            log.debug("Updating name to: {} {}", firstName, lastName);
            userBase.setName(Text.of(firstName), Text.of(lastName));
        }

        // Update image URL
        if (request.getImageURL() != null) {
            log.debug("Updating imageURL to: {}", request.getImageURL());
            userBase.setImageUrl(request.getImageURL());
        }

        // Update phone
        if (request.getPhone() != null) {
            log.debug("Updating phone to: {}", request.getPhone());
            userBase.setPhone(Text.of(request.getPhone()));
        }

        // Update region in profile
        if (request.getRegion() != null) {
            log.debug("Updating region to: {}", request.getRegion());
            UserProfile profile = userBase.getProfile(UserProfile.class).orElse(new UserProfile());
            profile.setCountry(request.getRegion());
            userBase.setProfile(profile);
        }


        // Retrieve updated user information
        UserVal updatedUser = userBase.getValue();
        Map<String, String> updatedPreferences = userBase.getPreference();

        // Get updated region from profile
        String updatedRegion = null;
        UserProfile updatedProfile = userBase.getProfile(UserProfile.class).orElse(null);
        if (updatedProfile != null) {
            updatedRegion = updatedProfile.getCountry();
        }
        FullUserProfileDto Dto = FullUserProfileDto.builder()
                .id(updatedUser.id().toString())
                .displayName(updatedUser.displayName())
                .firstName(updatedUser.firstName())
                .lastName(updatedUser.lastName())
                .email(updatedUser.email())
                .phone(updatedUser.phone())
                .imageURL(updatedUser.imageUrl())
                .region(updatedRegion)
                .preferences(updatedPreferences)
                .build();

        log.info("Full user profile updated successfully for user: {}", updatedUser.email());
        return Dto;
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
