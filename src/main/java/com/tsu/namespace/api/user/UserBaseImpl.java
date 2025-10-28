package com.tsu.namespace.api.user;

import com.tsu.auth.api.Login;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.common.exception.PermissionDeniedException;
import com.tsu.common.locale.EffectiveLocaleSettings;
import com.tsu.common.locale.EffectiveLocaleSettingsBuilder;
import com.tsu.common.utils.LazyCacheLoader;
import com.tsu.common.utils.ParamValidator;
import com.tsu.common.val.UserVal;
import com.tsu.common.vo.Email;
import com.tsu.common.vo.Text;
import com.tsu.enums.BaseConstants;
import com.tsu.enums.BaseParamName;
import com.tsu.namespace.api.UserBase;
import com.tsu.namespace.api.UserPreferences;
import com.tsu.namespace.api.UserProfile;
import com.tsu.namespace.api.formatter.FormatterImpl;
import com.tsu.namespace.helper.NamespaceDbHelper;
import com.tsu.namespace.record.NamespaceRecord;
import com.tsu.namespace.record.UserRecord;
import com.tsu.namespace.val.NamespaceVal;
import com.tsu.workspace.api.Formatter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
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

    public UserBaseImpl(UserRecord record, NamespaceDbHelper namespaceDbHelper, AppSecurityContext context) {
        this.record = record;
        this.context = context;
        this.namespaceDbHelper = namespaceDbHelper;
        this.namespaces = LazyCacheLoader.of(() -> namespaceDbHelper.findByPrincipalId(id(), context)
                .map(NamespaceRecord::getValue)
                .toList());
    }


    @Override
    public void setName(Text firstName, Text lastName) {
        log.info("Updating name for user ID: {} to '{} {}'", record.id(), firstName.strip(), lastName.strip());
        record.setName(firstName.strip(), lastName.strip());
        record.persist();
        log.debug("Name updated successfully for user ID: {}", record.id());
    }


    @Override
    public void setDisplayName(Text name) {
        log.info("Updating display name for user ID: {} to '{}'", record.id(), name.strip());
        ParamValidator.builder()
                .withNonNull(name, BaseParamName.DISPLAY_NAME)
                .throwIfErrors();
        record.setDisplayName(name.strip());
        record.persist();
        log.debug("Display name updated successfully for user ID: {}", record.id());

    }

    @Override
    public void setEmail(Email email) {
        log.info("Updating email for user ID: {} to '{}'", record.id(), email.validateAndGet());
        ParamValidator.builder()
                .withVerifyEmail(email)
                .throwIfErrors();
        record.setEmail(email.toString());
        record.persist();
        log.debug("Email updated successfully for user ID: {}", record.id());
    }

    @Override
    public void setPhone(Text phone) {
        log.info("Updating phone number for user ID: {}", record.id());
        ParamValidator.builder()
                .withNonNull(phone, BaseParamName.PHONE)
                .throwIfErrors();
        record.setPhone(phone.strip());
        record.persist();
        log.debug("Phone number updated successfully for user ID: {}", record.id());
    }

    @Override
    public String getEmail() {
        return record.getEmail();
    }


    @Override
    public UserPreferences getPreference() {
        return record.getPreferences();
    }


    @Override
    public void setPreferences(UserPreferences preference) {
        record.setPreferences(preference);
        record.persist();
    }


    @Override
    public void activate() {
        log.info("Attempting to activate user ID: {} by principal: {}", record.id(), context.getPrincipal().id());
        if (!Objects.equals(context.getPrincipal(), BaseConstants.SYSADMIN)) {
            log.warn("Permission denied: Non-admin user {} attempted to activate user {}", context.getPrincipal().id(), record.id());
            throw new PermissionDeniedException("Permission denied");
        }
        record.setActive(true);
        record.persist();
        log.info("User successfully activated: {}", record.id());
    }

    @Override
    public void deactivate() {
        if (!Objects.equals(context.getPrincipal(), BaseConstants.SYSADMIN)) {
            throw new PermissionDeniedException("Permission denied");
        }
        record.setActive(false);
        record.persist();

    }

    @Override
    public void setImageUrl(String url) {
        record.setImageUrl(url);
        record.persist();
    }


    @Override
    public Stream<Login> getLogins() {
        return Stream.empty();
    }

    @Override
    public UserProfile toProfile() {
        if(!Objects.equals(context.getPrincipal().id().toString(),id().toString())){
            throw new PermissionDeniedException("read other user's profile is not allowed");
        }
        UserVal val = record.getValue();
        return new UserProfile(val.id(), val.displayName(), val.firstName(), val.lastName(), val.email(), val.imageUrl(), val.active(),
                val.phone(), record.getTimezoneId(),
                record.getLanguageTag(), record.getDatePattern(), record.getDatetimePattern(), getPreference());
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
        EffectiveLocaleSettings localeSettings = new EffectiveLocaleSettingsBuilder()
                .languageTag(record.getLanguageTag())
                .timezoneId(record.getTimezoneId())
                .datePattern(record.getDatePattern())
                .datetimePattern(record.getDatetimePattern())
                .build();
        return new FormatterImpl(localeSettings);
    }

    @Override
    public UserVal getValue() {
        return record.getValue();
    }

    @Override
    public boolean isValid() {
        return record.isActive();
    }

    @Override
    public UUID id() {
        return record.id();
    }


    @Override
    public void setLocale(Locale locale) {
        record.setLanguageTag(Optional.ofNullable(locale).map(Locale::toLanguageTag).orElse(null));
        record.persist();
    }

    @Override
    public void setZone(ZoneId zone) {
        record.setTimezoneId(Optional.ofNullable(zone).map(ZoneId::getId).orElse(null));
        record.persist();
    }

    @Override
    public void setDateFormat(String dateFormat) {
        record.setDatePattern(dateFormat);
        record.persist();
    }

    @Override
    public void setDateTimeFormat(String dateTimeFormat) {
        record.setDatetimePattern(dateTimeFormat);
        record.persist();
    }

    @Override
    public EffectiveLocaleSettings getSettings() {
        return EffectiveLocaleSettingsBuilder.build(record.getLanguageTag(), record.getTimezoneId(), record.getDatePattern(), record.getDatetimePattern());
    }

    @Override
    public void setSettings(EffectiveLocaleSettings settings) {
        record.setLanguageTag(settings.getLanguageTag());
        record.setTimezoneId(settings.getTimezoneId());
        record.setDatePattern(settings.dateFormat());
        record.setDatetimePattern(settings.dateTimeFormat());
        record.persist();
    }
}
