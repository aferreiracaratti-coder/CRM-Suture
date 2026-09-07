package com.suture.crm.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

@ConfigurationProperties("suture.auth")
public class SutureAuthProperties {
    private String bootstrapEmail = "admin@suture.local";
    private String bootstrapName = "Administrador local";
    private String bootstrapPassword;
    private UUID bootstrapTenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    public String getBootstrapEmail() { return bootstrapEmail; }
    public void setBootstrapEmail(String bootstrapEmail) { this.bootstrapEmail = bootstrapEmail; }
    public String getBootstrapName() { return bootstrapName; }
    public void setBootstrapName(String bootstrapName) { this.bootstrapName = bootstrapName; }
    public String getBootstrapPassword() { return bootstrapPassword; }
    public void setBootstrapPassword(String bootstrapPassword) { this.bootstrapPassword = bootstrapPassword; }
    public UUID getBootstrapTenantId() { return bootstrapTenantId; }
    public void setBootstrapTenantId(UUID bootstrapTenantId) { this.bootstrapTenantId = bootstrapTenantId; }
}
