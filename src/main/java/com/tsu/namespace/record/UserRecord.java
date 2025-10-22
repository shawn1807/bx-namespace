package com.tsu.namespace.record;

import com.tsu.namespace.api.UserProfile;
import com.tsu.namespace.entities.UserBaseTb;
import com.tsu.common.val.UserVal;
import com.tsu.common.jpa.JsonValueUtils;
import com.tsu.common.utils.ValueObserver;
import com.tsu.auth.security.AppSecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;


@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class UserRecord {

    @ToString.Include
    private final UserBaseTb tb;
    private final Consumer<UserBaseTb> persist;
    private final ValueObserver<String> nameObserver;

    public void persist(AppSecurityContext context) {
        tb.setModifiedBy(context.getPrincipal().id());
        tb.setModifiedDate(LocalDateTime.now());
        persist.accept(tb);
    }

    public String getEmail(){
        return tb.getEmail();
    }

    public String getDisplayName() {
        return tb.getDisplayName();
    }

    public void setProfile(Object profile) {
        tb.setProfile(JsonValueUtils.getInstance().encodeAsJson(profile));
    }

    public <T extends UserProfile> T getProfile(Class<T> type) {
        return JsonValueUtils.getInstance().decode(tb.getProfile(), type);
    }

    public Map<String, String> getPreference() {
        return JsonValueUtils.getInstance().decode(tb.getPreference());
    }

    public void setPreferences(Map<String, String> preference) {
        tb.setPreference(JsonValueUtils.getInstance().encodeAsJson(preference));
    }

    public UUID getId() {
        return tb.getId();
    }

    public void setName(String firstName,String lastName) {
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
        return new UserVal(tb.getId(), tb.getDisplayName(), tb.getFirstName(), tb.getLastName(),tb.getPhone(), tb.getEmail(), tb.getImageUrl());
    }

    public boolean isActive() {
        return tb.isActive();
    }

    public LocalDate getExpirationDate() {
        return tb.getExpirationDate();
    }

    public void setActive(boolean active) {
        tb.setActive(active);
    }

    public void setImageUrl(String url) {
        tb.setImageUrl(url);
    }
}
