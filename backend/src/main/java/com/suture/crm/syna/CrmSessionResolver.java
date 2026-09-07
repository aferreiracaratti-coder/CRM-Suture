package com.suture.crm.syna;

import com.suture.crm.auth.CrmUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CrmSessionResolver {
    private final SynaEmbeddedProperties properties;
    private final CrmUserRepository users;

    CrmSessionResolver(SynaEmbeddedProperties properties, CrmUserRepository users) {
        this.properties = properties;
        this.users = users;
    }

    public CrmSessionPrincipal requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return users.findByEmailIgnoreCase(authentication.getName())
                    .filter(user -> user.isActive())
                    .map(user -> new CrmSessionPrincipal(user.getId().toString(), user.getDisplayName(), user.roleList()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "An authenticated CRM session is required"));
        }
        if (!properties.isLocalIdentityEnabled()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "An authenticated CRM session is required");
        }
        if (!StringUtils.hasText(properties.getLocalUserId()) || !StringUtils.hasText(properties.getLocalUserName())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "The local CRM identity is not configured");
        }
        return new CrmSessionPrincipal(properties.getLocalUserId(), properties.getLocalUserName(), properties.getLocalRoles());
    }
}
