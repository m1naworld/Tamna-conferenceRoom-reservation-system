package com.example.tamna.config;

import io.swagger.models.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer{

    @Value("${AUTHORIZATION_HEADER}")
    private String ACCESSTOKEN_HEADER;

    @Value("${REAUTHORIZATION_HEADER}")
    private String REFRESHTOKEN_HEADER;

    @Value("${ADMIN_HEADER}")
    private String ADMINTOKEN_HEADER;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedOriginPatterns("*")
                .allowedMethods(HttpMethod.GET.name(), HttpMethod.POST.name())
                .allowedHeaders("*")
                .exposedHeaders(ACCESSTOKEN_HEADER, REFRESHTOKEN_HEADER, ADMINTOKEN_HEADER)
                .maxAge(3000);
        WebMvcConfigurer.super.addCorsMappings(registry);

    }
}
