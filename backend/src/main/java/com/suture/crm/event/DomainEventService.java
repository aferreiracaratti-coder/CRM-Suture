package com.suture.crm.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class DomainEventService {
    private final JdbcClient jdbc;
    private final ObjectMapper objectMapper;
    DomainEventService(JdbcClient jdbc, ObjectMapper objectMapper) { this.jdbc = jdbc; this.objectMapper = objectMapper; }

    public void publish(UUID tenantId, String type, String aggregateType, UUID aggregateId, String name) {
        jdbc.sql("""
                INSERT INTO domain_event (tenant_id, type, aggregate_type, aggregate_id, payload)
                VALUES (:tenantId, :type, :aggregateType, :aggregateId, CAST(:payload AS jsonb))
                """).param("tenantId", tenantId).param("type", type).param("aggregateType", aggregateType)
                .param("aggregateId", aggregateId).param("payload", jsonFor(name)).update();
    }
    private String jsonFor(String name) {
        try { return objectMapper.writeValueAsString(Map.of("name", name)); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("No se pudo serializar el evento", exception); }
    }
}
