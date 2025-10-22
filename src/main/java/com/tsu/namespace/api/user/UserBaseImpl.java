package com.tsu.namespace.api.user;

import com.tsu.common.exception.PermissionDeniedException;
import com.tsu.common.jpa.JsonValueUtils;
import com.tsu.common.utils.LazyCacheLoader;
import com.tsu.common.utils.ParamValidator;
import com.tsu.common.vo.Email;
import com.tsu.common.vo.Text;
import com.tsu.entry.api.AclMode;
import com.tsu.entry.api.EntryBucket;
import com.tsu.entry.service.BucketService;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.enums.BaseConstants;
import com.tsu.enums.BaseParamName;
import com.tsu.namespace.helper.NamespaceDbHelper;
import com.tsu.namespace.record.NamespaceRecord;
import com.tsu.namespace.record.UserRecord;
import com.tsu.workspace.api.Formatter;
import com.tsu.auth.api.Login;
import com.tsu.namespace.api.UserBase;
import com.tsu.namespace.api.UserProfile;
import com.tsu.namespace.val.NamespaceVal;
import com.tsu.common.val.UserVal;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
public class UserBaseImpl implements UserBase {

    @ToString.Include
    private final UserRecord record;
    private final AppSecurityContext context;
    private final NamespaceDbHelper namespaceDbHelper;
    private final LazyCacheLoader<List<NamespaceVal>> namespaces;
    private final LazyCacheLoader<EntryBucket> bucket;

    public UserBaseImpl(UserRecord record, BucketService bucketService, NamespaceDbHelper namespaceDbHelper, AppSecurityContext context) {
        this.record = record;
        this.context = context;
        this.namespaceDbHelper = namespaceDbHelper;
        this.namespaces = LazyCacheLoader.of(() -> namespaceDbHelper.findByPrincipalId(id(), context)
                .map(NamespaceRecord::getValue)
                .toList());
        this.bucket = LazyCacheLoader.of(() -> bucketService.findBucket(record.id().toString(), context.getBucketContext())
                .orElseGet(() -> bucketService.createBucket(record.id().toString(), BaseConstants.SYSTEM_BUCKET_PROVIDER, context.getBucketContext(), AclMode.FULL))
        );
    }


    @Override
    public <T extends UserProfile> Optional<T> getProfile(Class<T> type) {
        return Optional.ofNullable(record.getProfile(type));
    }

    @Override
    public <T extends UserProfile> void setProfile(T profile) {
        log.info("Updating user profile for user ID: {}", record.id());
        log.debug("Setting profile of type: {}", profile.getClass().getSimpleName());
        record.setProfile(JsonValueUtils.getInstance().encodeAsJson(profile));
        record.persist(context);
        log.debug("User profile updated successfully for user ID: {}", record.id());
    }

    @Override
    public void setName(Text firstName, Text lastName) {
        log.info("Updating name for user ID: {} to '{} {}'", record.id(), firstName.strip(), lastName.strip());
        record.setName(firstName.strip(), lastName.strip());
        record.persist(context);
        log.debug("Name updated successfully for user ID: {}", record.id());
    }


    @Override
    public void setDisplayName(Text name) {
        log.info("Updating display name for user ID: {} to '{}'", record.id(), name.strip());
        ParamValidator.builder()
                .withNonNull(name, BaseParamName.DISPLAY_NAME)
                .throwIfErrors();
        record.setDisplayName(name.strip());
        record.persist(context);
        log.debug("Display name updated successfully for user ID: {}", record.id());

    }

    @Override
    public void setEmail(Email email) {
        log.info("Updating email for user ID: {} to '{}'", record.id(), email.validateAndGet());
        ParamValidator.builder()
                .withVerifyEmail(email)
                .throwIfErrors();
        record.setEmail(email.toString());
        record.persist(context);
        log.debug("Email updated successfully for user ID: {}", record.id());
    }

    @Override
    public void setPhone(Text phone) {
        log.info("Updating phone number for user ID: {}", record.id());
        ParamValidator.builder()
                .withNonNull(phone, BaseParamName.PHONE)
                .throwIfErrors();
        record.setPhone(phone.strip());
        record.persist(context);
        log.debug("Phone number updated successfully for user ID: {}", record.id());
    }

    @Override
    public String getEmail() {
        return record.getEmail();
    }


    @Override
    public Map<String, String> getPreference() {
        return record.getPreference();
    }

    @Override
    public void setPreferences(Map<String, String> preference) {
        record.setPreferences(preference);
        record.persist(context);
    }

    @Override
    public void removePreferences(String... keys) {
        Map<String, String> preferences = getPreference();
        Stream.ofNullable(keys)
                .flatMap(Arrays::stream)
                .forEach(preferences::remove);
        setPreferences(preferences);
    }


    @Override
    public void activate() {
        log.info("Attempting to activate user ID: {} by principal: {}", record.id(), context.getPrincipal().id());
        if (!Objects.equals(context.getPrincipal(), BaseConstants.SYSADMIN)) {
            log.warn("Permission denied: Non-admin user {} attempted to activate user {}", context.getPrincipal().id(), record.id());
            throw new PermissionDeniedException("Permission denied");
        }
        record.setActive(true);
        record.persist(context);
        log.info("User successfully activated: {}", record.id());
    }

    @Override
    public void deactivate() {
        if (!Objects.equals(context.getPrincipal(), BaseConstants.SYSADMIN)) {
            throw new PermissionDeniedException("Permission denied");
        }
        record.setActive(false);
        record.persist(context);

    }

    @Override
    public void setImageUrl(String url) {
        record.setImageUrl(url);
        record.persist(context);
    }


    @Override
    public Stream<Login> getLogins() {
        return Stream.empty();
    }

    @Override
    public EntryBucket getBucket() {
        return bucket.get();
    }

    @Override
    public Stream<NamespaceVal> findNamespaces() {
        return namespaces.get().stream();
    }

    @Override
    public Optional<NamespaceVal> findNamespace(UUID namespaceId) {
        return namespaceDbHelper.findByNamespaceIdAndUserId(namespaceId, id(), context)
                .map(NamespaceRecord::getValue);
    }


    @Override
    public Formatter getFormatter() {
        return new UserDataFormatter(record);
    }

    @Override
    public UserVal getValue() {
        return record.getValue();
    }

    @Override
    public boolean isValid() {
        if (record.isActive()) {
            return Optional.ofNullable(record.getExpirationDate())
                    .map(expiry -> LocalDate.now().isAfter(expiry))
                    .orElse(false);
        }
        return false;
    }

    @Override
    public UUID id() {
        return record.id();
    }

}
