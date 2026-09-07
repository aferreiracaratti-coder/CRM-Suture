package com.suture.crm.auth;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CrmUserPrincipal implements UserDetails {
    private final UUID id;
    private final UUID tenantId;
    private final String email;
    private final String displayName;
    private final String passwordHash;
    private final List<SimpleGrantedAuthority> authorities;
    private final boolean active;

    public CrmUserPrincipal(CrmUser user) {
        this.id = user.getId();
        this.tenantId = user.getTenantId();
        this.email = user.getEmail();
        this.displayName = user.getDisplayName();
        this.passwordHash = user.getPasswordHash();
        this.authorities = user.roleList().stream().map(SimpleGrantedAuthority::new).toList();
        this.active = user.isActive();
    }

    public UUID id() { return id; }
    public UUID tenantId() { return tenantId; }
    public String displayName() { return displayName; }
    public List<String> roles() { return authorities.stream().map(GrantedAuthority::getAuthority).toList(); }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return active; }
    @Override public boolean isAccountNonLocked() { return active; }
    @Override public boolean isCredentialsNonExpired() { return active; }
    @Override public boolean isEnabled() { return active; }
}
