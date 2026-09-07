package com.suture.crm.crm.lead;

import com.suture.crm.audit.AuditTrailService;
import com.suture.crm.common.ResourceNotFoundException;
import com.suture.crm.crm.company.CompanyRepository;
import com.suture.crm.event.DomainEventService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrmLeadService {
    private final JdbcClient jdbc;
    private final CompanyRepository companies;
    private final AuditTrailService audit;
    private final DomainEventService events;

    CrmLeadService(JdbcClient jdbc, CompanyRepository companies, AuditTrailService audit, DomainEventService events) {
        this.jdbc = jdbc;
        this.companies = companies;
        this.audit = audit;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<CrmLeadResponse> list(UUID tenantId) {
        return jdbc.sql(sql())
                .param("tenantId", tenantId)
                .query(this::map)
                .list();
    }

    @Transactional
    public CrmLeadResponse create(UUID tenantId, UUID actorId, CreateCrmLeadRequest request) {
        if (request.companyId() == null || !companies.existsByIdAndTenantId(request.companyId(), tenantId)) {
            throw new ResourceNotFoundException("Empresa no encontrada");
        }
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO lead (id, tenant_id, company_id, contact_id, source, temperature, status,
                    next_contact_at, score, summary, priority, data_quality, owner_id, created_by_id)
                VALUES (:id, :tenantId, :companyId, :contactId, :source, :temperature, :status,
                    :nextContactAt, 0, :summary, :priority, :dataQuality, :ownerId, :createdBy)
                """)
                .param("id", id).param("tenantId", tenantId).param("companyId", request.companyId())
                .param("contactId", request.contactId()).param("source", blankToNull(request.source()))
                .param("temperature", normalize(request.temperature(), "COLD")).param("status", normalize(request.status(), "NEW"))
                .param("nextContactAt", request.nextContactAt()).param("summary", blankToNull(request.summary()))
                .param("priority", blankToNull(request.priority())).param("dataQuality", normalize(request.dataQuality(), "HIGH"))
                .param("ownerId", actorId).param("createdBy", actorId)
                .update();
        events.publish(tenantId, "LEAD_CREATED", "LEAD", id, request.summary() == null ? "Lead" : request.summary());
        audit.recordUserAction(tenantId, actorId, "LEAD_CREATED", "LEAD", id, request.summary() == null ? "Lead" : request.summary());
        return find(tenantId, id);
    }

    @Transactional
    public void delete(UUID tenantId, UUID actorId, UUID id) {
        CrmLeadResponse existing = find(tenantId, id);
        jdbc.sql("DELETE FROM lead WHERE tenant_id = :tenantId AND id = :id")
                .param("tenantId", tenantId).param("id", id).update();
        String name = existing.summary() == null || existing.summary().isBlank() ? existing.companyName() : existing.summary();
        events.publish(tenantId, "LEAD_DELETED", "LEAD", id, name);
        audit.recordUserAction(tenantId, actorId, "LEAD_DELETED", "LEAD", id, name);
    }

    private CrmLeadResponse find(UUID tenantId, UUID id) {
        return jdbc.sql(sql() + " AND l.id = :id")
                .param("tenantId", tenantId).param("id", id)
                .query(this::map).optional()
                .orElseThrow(() -> new ResourceNotFoundException("Lead no encontrado"));
    }

    private String sql() {
        return """
                SELECT l.id, l.company_id, co.name AS company_name, l.contact_id, l.source,
                       l.temperature, l.status, l.priority, l.data_quality, l.next_contact_at,
                       l.score, l.summary, l.created_at, l.updated_at
                  FROM lead l
                  JOIN company co ON co.id = l.company_id
                 WHERE l.tenant_id = :tenantId
                 ORDER BY l.updated_at DESC
                """;
    }

    private CrmLeadResponse map(ResultSet rs, int ignored) throws SQLException {
        return new CrmLeadResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("company_id", UUID.class),
                rs.getString("company_name"),
                rs.getObject("contact_id", UUID.class),
                rs.getString("source"),
                rs.getString("temperature"),
                rs.getString("status"),
                rs.getString("priority"),
                rs.getString("data_quality"),
                rs.getObject("next_contact_at", OffsetDateTime.class),
                rs.getObject("score", Integer.class),
                rs.getString("summary"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getObject("updated_at", OffsetDateTime.class));
    }

    private String normalize(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim().toUpperCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record CreateCrmLeadRequest(
            UUID companyId,
            UUID contactId,
            String status,
            String temperature,
            String priority,
            String source,
            String summary,
            String dataQuality,
            OffsetDateTime nextContactAt) { }

    public record CrmLeadResponse(
            UUID id,
            UUID companyId,
            String companyName,
            UUID contactId,
            String source,
            String temperature,
            String status,
            String priority,
            String dataQuality,
            OffsetDateTime nextContactAt,
            Integer score,
            String summary,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) { }
}
