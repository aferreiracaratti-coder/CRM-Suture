package com.suture.crm.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class AuditTrailService {
    private final JdbcClient jdbc;
    private final ObjectMapper objectMapper;
    AuditTrailService(JdbcClient jdbc, ObjectMapper objectMapper) { this.jdbc = jdbc; this.objectMapper = objectMapper; }

    public void recordSystemAction(UUID tenantId, String action, String aggregateType, UUID aggregateId, String name) {
        jdbc.sql("""
                INSERT INTO audit_event (tenant_id, actor_type, actor_id, action, aggregate_type, aggregate_id, data)
                VALUES (:tenantId, 'SYSTEM', 'bootstrap', :action, :aggregateType, :aggregateId, CAST(:data AS jsonb))
                """).param("tenantId", tenantId).param("action", action).param("aggregateType", aggregateType)
                .param("aggregateId", aggregateId).param("data", jsonFor(name)).update();
    }
    public void recordAgentAction(UUID tenantId, String actorId, String action, String aggregateType, UUID aggregateId, String name) {
        jdbc.sql("""
                INSERT INTO audit_event (tenant_id, actor_type, actor_id, action, aggregate_type, aggregate_id, data)
                VALUES (:tenantId, 'AGENT', :actorId, :action, :aggregateType, :aggregateId, CAST(:data AS jsonb))
                """).param("tenantId", tenantId).param("actorId", actorId).param("action", action)
                .param("aggregateType", aggregateType).param("aggregateId", aggregateId).param("data", jsonFor(name)).update();
    }
    private String jsonFor(String name) {
        try { return objectMapper.writeValueAsString(Map.of("name", name)); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("No se pudo serializar la auditoría", exception); }
    }
}
