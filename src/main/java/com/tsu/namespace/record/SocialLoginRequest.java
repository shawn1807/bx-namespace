package com.tsu.namespace.record;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SocialLoginRequest(
    @NotBlank(message = "Token is required")
    String token,

    @NotNull(message = "Provider is required")
    SocialProvider provider
) {
    public enum SocialProvider {
        GOOGLE,
        FACEBOOK
    }
}