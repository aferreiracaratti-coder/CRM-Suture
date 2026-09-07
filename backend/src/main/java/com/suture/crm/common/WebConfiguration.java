package com.suture.crm.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    private final String[] frontendOrigins;
    WebConfiguration(@Value("${SUTURE_CORS_ALLOWED_ORIGIN:http://localhost:5173,http://127.0.0.1:15173}") String frontendOrigins) {
        this.frontendOrigins = Arrays.stream(frontendOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }
    @Override public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(frontendOrigins).allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE").allowedHeaders("*");
    }
}
