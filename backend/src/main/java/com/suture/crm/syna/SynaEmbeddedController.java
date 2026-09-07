package com.suture.crm.syna;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class SynaEmbeddedController {
    private static final Logger log = LoggerFactory.getLogger(SynaEmbeddedController.class);
    private final SynaEmbeddedProperties properties;
    private final CrmSessionResolver sessions;
    private final SynaJwtService jwt;
    private final RestClient.Builder clientBuilder;

    SynaEmbeddedController(SynaEmbeddedProperties properties, CrmSessionResolver sessions, SynaJwtService jwt, RestClient.Builder clientBuilder) {
        this.properties = properties;
        this.sessions = sessions;
        this.jwt = jwt;
        this.clientBuilder = clientBuilder;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() { return jwt.jwks(); }

    @PostMapping(path = "/api/syna/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> chat(@Valid @RequestBody SynaChatRequest request) {
        if (!properties.isEnabled() || !StringUtils.hasText(properties.getBaseUrl())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "The Syna embedded integration is not configured");
        }
        CrmSessionPrincipal principal = sessions.requireCurrentUser();
        Map<String, String> context = sanitizeContext(request.context());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("conversationId", request.conversationId());
        payload.put("message", request.message());
        payload.put("mode", "SALES");
        payload.put("reasoningProfile", "NORMAL");
        payload.put("context", context);
        try {
            String response = clientBuilder.clone().baseUrl(properties.getBaseUrl()).build().post().uri("/v1/chat")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.issue(principal))
                    .contentType(MediaType.APPLICATION_JSON).body(payload).retrieve().body(String.class);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
        } catch (Exception exception) {
            log.warn("Embedded Syna chat request failed: {}", exception.toString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Syna is unavailable");
        }
    }

    @GetMapping("/api/syna/conversations/{conversationId}")
    public ResponseEntity<String> conversation(@PathVariable UUID conversationId) {
        if (!properties.isEnabled() || !StringUtils.hasText(properties.getBaseUrl())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "The Syna embedded integration is not configured");
        }
        CrmSessionPrincipal principal = sessions.requireCurrentUser();
        try {
            String response = clientBuilder.clone().baseUrl(properties.getBaseUrl()).build().get()
                    .uri("/v1/conversations/{conversationId}", conversationId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.issue(principal))
                    .retrieve().body(String.class);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Syna conversation not found");
            }
            log.warn("Embedded Syna conversation request failed: {}", exception.toString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Syna is unavailable");
        } catch (Exception exception) {
            log.warn("Embedded Syna conversation request failed: {}", exception.toString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Syna is unavailable");
        }
    }

    @GetMapping("/api/syna/actions")
    public ResponseEntity<String> actions(@RequestParam(defaultValue = "PENDING") String status) {
        return relayGet("/v1/actions?status={status}", status);
    }

    @PostMapping("/api/syna/actions/{actionId}/approve")
    public ResponseEntity<String> approve(@PathVariable UUID actionId) {
        return relayPost("/v1/actions/{actionId}/approve", actionId);
    }

    @PostMapping("/api/syna/actions/{actionId}/reject")
    public ResponseEntity<String> reject(@PathVariable UUID actionId) {
        return relayPost("/v1/actions/{actionId}/reject", actionId);
    }

    private ResponseEntity<String> relayGet(String path, Object... variables) {
        requireConfigured();
        try {
            String response = clientBuilder.clone().baseUrl(properties.getBaseUrl()).build().get().uri(path, variables)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.issue(sessions.requireCurrentUser()))
                    .retrieve().body(String.class);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
        } catch (RestClientResponseException exception) {
            throw relayedFailure(exception, "Syna could not load pending actions");
        } catch (Exception exception) {
            log.warn("Embedded Syna action request failed: {}", exception.toString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Syna is unavailable");
        }
    }

    private ResponseEntity<String> relayPost(String path, Object... variables) {
        requireConfigured();
        try {
            String response = clientBuilder.clone().baseUrl(properties.getBaseUrl()).build().post().uri(path, variables)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.issue(sessions.requireCurrentUser()))
                    .retrieve().body(String.class);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
        } catch (RestClientResponseException exception) {
            throw relayedFailure(exception, "Syna could not update this action");
        } catch (Exception exception) {
            log.warn("Embedded Syna action request failed: {}", exception.toString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Syna is unavailable");
        }
    }

    private void requireConfigured() {
        if (!properties.isEnabled() || !StringUtils.hasText(properties.getBaseUrl())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "The Syna embedded integration is not configured");
        }
    }

    private ResponseStatusException relayedFailure(RestClientResponseException exception, String fallback) {
        String detail = StringUtils.hasText(exception.getResponseBodyAsString())
                ? exception.getResponseBodyAsString() : fallback;
        return new ResponseStatusException(HttpStatus.valueOf(exception.getStatusCode().value()), detail);
    }

    private Map<String, String> sanitizeContext(Map<String, String> context) {
        if (context == null || context.isEmpty()) return Map.of();
        Map<String, String> safe = new LinkedHashMap<>();
        copy(context, safe, "entityType", 40);
        copy(context, safe, "entityId", 255);
        copy(context, safe, "screen", 160);
        return Map.copyOf(safe);
    }

    private void copy(Map<String, String> source, Map<String, String> target, String key, int maxLength) {
        String value = source.get(key);
        if (StringUtils.hasText(value) && value.length() <= maxLength) target.put(key, value.trim());
    }

    public record SynaChatRequest(UUID conversationId, @NotBlank @Size(max = 8000) String message, Map<String, String> context) { }
}
