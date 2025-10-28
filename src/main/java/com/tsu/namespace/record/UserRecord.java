package com.tsu.namespace.record;

import com.tsu.common.jpa.JsonValueUtils;
import com.tsu.common.utils.ValueObserver;
import com.tsu.common.val.UserVal;
import com.tsu.namespace.api.UserPreferences;
import com.tsu.namespace.entities.UserBaseTb;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;


@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class UserRecord {

    @ToString.Include
    private final UserBaseTb tb;
    private final Consumer<UserBaseTb> persist;
    private final ValueObserver<String> nameObserver;

    public void persist() {
        tb.setModifiedDate(LocalDateTime.now());
        persist.accept(tb);
    }

    public String getEmail() {
        return tb.getEmail();
    }

    public String getDisplayName() {
        return tb.getDisplayName();
    }


    public UserPreferences getPreferences() {
        return JsonValueUtils.getInstance().decode(tb.getPreferences(), UserPreferences.class);
    }

    public void setPreferences(UserPreferences preferences) {
        tb.setPreferences(JsonValueUtils.getInstance().encodeAsJson(preferences));
    }

    public UUID getId() {
        return tb.getId();
    }

    public void setName(String firstName, String lastName) {
        tb.setFirstName(firstName);
        tb.setLastName(lastName);
    }

    public void setDisplayName(String displayName) {
        tb.setDisplayName(displayName);
        nameObserver.updated(displayName);
    }


    public void setEmail(String email) {
        tb.setEmail(email);
    }

    public void setPhone(String phone) {
        tb.setPhone(phone);
    }

    public UUID id() {
        return tb.getId();
    }

    public String displayName() {
        return tb.getDisplayName();
    }

    public UserVal getValue() {
        return new UserVal(tb.getId(), tb.getDisplayName(), tb.getFirstName(), tb.getLastName(), tb.getPhone(), tb.getEmail(), tb.getImageUrl(), tb.isActive());
    }

    public boolean isActive() {
        return tb.isActive();
    }

    public void setActive(boolean active) {
        tb.setActive(active);
    }

    public void setImageUrl(String url) {
        tb.setImageUrl(url);
    }


    public String getLanguageTag() {
        return tb.getLanguageTag();
    }

    public void setLanguageTag(String languageTag) {
        tb.setLanguageTag(languageTag);
    }

    public String getTimezoneId() {
        return tb.getTimezoneId();
    }

    public void setTimezoneId(String timezoneId) {
        tb.setTimezoneId(timezoneId);
    }

    public String getDatePattern() {
        return tb.getDatePattern();
    }

    public void setDatePattern(String datePattern) {
        tb.setDatePattern(datePattern);
    }

    public String getDatetimePattern() {
        return tb.getDatetimePattern();
    }

    public void setDatetimePattern(String datetimePattern) {
        tb.setDatetimePattern(datetimePattern);
    }
}
