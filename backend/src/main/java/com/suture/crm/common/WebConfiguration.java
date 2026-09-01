package com.suture.crm.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    private final String frontendOrigin;
    WebConfiguration(@Value("${SUTURE_CORS_ALLOWED_ORIGIN:http://localhost:5173}") String frontendOrigin) { this.frontendOrigin = frontendOrigin; }
    @Override public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(frontendOrigin).allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE").allowedHeaders("*");
    }
}
