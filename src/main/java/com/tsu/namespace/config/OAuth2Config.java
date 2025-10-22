package com.tsu.namespace.config;

import com.tsu.namespace.service.OAuth2LoginService;
import com.tsu.namespace.service.SocialJwtTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuth2Config {

    @Bean
    public SocialJwtTokenService socialJwtTokenService() {
        return new SocialJwtTokenService();
    }

    @Bean
    public OAuth2LoginService oAuth2LoginService(SocialJwtTokenService jwtTokenService) {
        return new OAuth2LoginService(jwtTokenService);
    }
}