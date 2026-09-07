package com.suture.crm.syna;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SynaJwtService {
    private final SynaEmbeddedProperties properties;
    private final ObjectMapper objectMapper;

    SynaJwtService(SynaEmbeddedProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String issue(CrmSessionPrincipal principal) {
        requireEnabled();
        Instant now = Instant.now();
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "RS256");
        header.put("typ", "JWT");
        header.put("kid", properties.getKeyId());
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", properties.getIssuer());
        claims.put("aud", List.of("syna-embedded"));
        claims.put("sub", principal.subject());
        claims.put("organization_id", properties.getOrganizationId().toString());
        claims.put("name", principal.name());
        claims.put("roles", principal.roles());
        claims.put("iat", now.getEpochSecond());
        claims.put("exp", now.plusSeconds(300).getEpochSecond());
        claims.put("jti", UUID.randomUUID().toString());
        String signingInput = encode(json(header).getBytes(StandardCharsets.UTF_8)) + "."
                + encode(json(claims).getBytes(StandardCharsets.UTF_8));
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey());
            signature.update(signingInput.getBytes(StandardCharsets.US_ASCII));
            return signingInput + "." + encode(signature.sign());
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign the Syna embedded JWT", exception);
        }
    }

    public Map<String, Object> jwks() {
        RSAPrivateCrtKey key = privateKey();
        return Map.of("keys", List.of(Map.of(
                "kty", "RSA", "use", "sig", "alg", "RS256", "kid", properties.getKeyId(),
                "n", encode(unsigned(key.getModulus().toByteArray())),
                "e", encode(unsigned(key.getPublicExponent().toByteArray()))
        )));
    }

    private RSAPrivateCrtKey privateKey() {
        requireEnabled();
        try {
            String encoded = properties.getPrivateKey();
            if (!StringUtils.hasText(encoded)) {
                throw unavailable("SYNA_EMBEDDED_PRIVATE_KEY is required");
            }
            String normalized = encoded.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
            PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(normalized)));
            if (!(key instanceof RSAPrivateCrtKey rsaKey)) {
                throw unavailable("The Syna private key must be an RSA PKCS#8 key");
            }
            return rsaKey;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw unavailable("SYNA_EMBEDDED_PRIVATE_KEY is invalid");
        }
    }

    private void requireEnabled() {
        if (!properties.isEnabled() || !StringUtils.hasText(properties.getIssuer()) || properties.getOrganizationId() == null) {
            throw unavailable("The Syna embedded integration is not configured");
        }
    }

    private String json(Map<String, Object> value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Could not encode the JWT", exception); }
    }

    private String encode(byte[] value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value); }
    private byte[] unsigned(byte[] value) { return value.length > 1 && value[0] == 0 ? java.util.Arrays.copyOfRange(value, 1, value.length) : value; }
    private ResponseStatusException unavailable(String reason) { return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, reason); }
}
