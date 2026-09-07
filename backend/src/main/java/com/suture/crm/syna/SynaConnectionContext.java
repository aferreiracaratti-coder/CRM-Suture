package com.suture.crm.syna;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record SynaConnectionContext(UUID organizationId, UUID tenantId, String permissions) {
    static final String ATTRIBUTE = SynaConnectionContext.class.getName();

    public static SynaConnectionContext require(HttpServletRequest request) {
        Object context = request.getAttribute(ATTRIBUTE);
        if (context instanceof SynaConnectionContext synaContext) {
            return synaContext;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Syna service authentication is required");
    }
}
