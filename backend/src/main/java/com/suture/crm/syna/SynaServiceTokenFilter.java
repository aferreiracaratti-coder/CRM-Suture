package com.suture.crm.syna;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SynaServiceTokenFilter extends OncePerRequestFilter {
    private static final int REQUESTS_PER_MINUTE = 120;
    private final JdbcClient jdbc;
    private final Map<String, RateWindow> rateWindows = new ConcurrentHashMap<>();

    SynaServiceTokenFilter(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean readable = "GET".equals(request.getMethod()) && (path.equals("/api/leads") || path.startsWith("/api/leads/")
                || path.equals("/api/customers") || path.startsWith("/api/customers/")
                || path.equals("/api/tasks") || path.startsWith("/api/tasks/")
                || path.equals("/api/deals") || path.startsWith("/api/deals/"));
        boolean taskWrite = "POST".equals(request.getMethod()) && path.equals("/api/tasks");
        return !(readable || taskWrite);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = bearerToken(request.getHeader("Authorization"));
        if (token == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "A bearer token is required");
            return;
        }
        String tokenHash = sha256(token);
        Optional<SynaConnectionContext> context = jdbc.sql("""
                SELECT organization_id, tenant_id, permissions
                  FROM syna_service_token
                 WHERE token_hash = :tokenHash AND revoked_at IS NULL
                """).param("tokenHash", tokenHash).query((rs, ignored) -> new SynaConnectionContext(
                rs.getObject("organization_id", UUID.class), rs.getObject("tenant_id", UUID.class), rs.getString("permissions"))).optional();
        if (context.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The service token is invalid");
            return;
        }
        UUID headerOrganization = organizationId(request.getHeader("X-Syna-Organization-Id"));
        if (headerOrganization == null || !headerOrganization.equals(context.get().organizationId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "The Syna organization does not match the token");
            return;
        }
        if ("POST".equals(request.getMethod()) && !"WRITE_TASKS".equals(context.get().permissions())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "This Syna token cannot create tasks");
            return;
        }
        if (!allow(tokenHash)) {
            response.setHeader("Retry-After", "60");
            response.sendError(429, "Rate limit exceeded");
            return;
        }
        request.setAttribute(SynaConnectionContext.ATTRIBUTE, context.get());
        chain.doFilter(request, response);
    }

    private String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring("Bearer ".length()).trim();
        return token.isEmpty() ? null : token;
    }

    private UUID organizationId(String raw) {
        try {
            return raw == null ? null : UUID.fromString(raw);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String sha256(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte valueByte : bytes) {
                result.append(String.format("%02x", valueByte));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private boolean allow(String tokenHash) {
        Instant now = Instant.now();
        RateWindow window = rateWindows.computeIfAbsent(tokenHash, ignored -> new RateWindow(now));
        synchronized (window) {
            if (now.isAfter(window.startedAt.plusSeconds(60))) {
                window.startedAt = now;
                window.requests = 0;
            }
            if (window.requests >= REQUESTS_PER_MINUTE) {
                return false;
            }
            window.requests++;
            return true;
        }
    }

    private static final class RateWindow {
        private Instant startedAt;
        private int requests;

        private RateWindow(Instant startedAt) {
            this.startedAt = startedAt;
        }
    }
}
