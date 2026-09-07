package com.suture.crm.syna;

import java.util.List;
import java.util.UUID;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("syna.embedded")
public class SynaEmbeddedProperties {
    private boolean enabled;
    private String issuer;
    private String baseUrl;
    private UUID organizationId;
    private String privateKey;
    private String keyId = "suture-crm-local";
    private boolean localIdentityEnabled;
    private String localUserId;
    private String localUserName;
    private List<String> localRoles = List.of("SYNA_SALES");

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }
    public boolean isLocalIdentityEnabled() { return localIdentityEnabled; }
    public void setLocalIdentityEnabled(boolean localIdentityEnabled) { this.localIdentityEnabled = localIdentityEnabled; }
    public String getLocalUserId() { return localUserId; }
    public void setLocalUserId(String localUserId) { this.localUserId = localUserId; }
    public String getLocalUserName() { return localUserName; }
    public void setLocalUserName(String localUserName) { this.localUserName = localUserName; }
    public List<String> getLocalRoles() { return localRoles; }
    public void setLocalRoles(List<String> localRoles) { this.localRoles = localRoles; }
}
