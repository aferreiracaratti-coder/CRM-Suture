package com.suture.crm.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "crm_user")
public class CrmUser {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(nullable = false) private String email;
    @Column(name = "display_name", nullable = false) private String displayName;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Column(nullable = false) private String roles;
    @Column(nullable = false) private boolean active;

    protected CrmUser() { }

    public CrmUser(UUID id, UUID tenantId, String email, String displayName, String passwordHash, String roles, boolean active) {
        this.id = id;
        this.tenantId = tenantId;
        this.email = email.toLowerCase();
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.roles = roles;
        this.active = active;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isActive() { return active; }
    public List<String> roleList() { return Arrays.stream(roles.split(",")).map(String::trim).filter(role -> !role.isEmpty()).toList(); }
}
