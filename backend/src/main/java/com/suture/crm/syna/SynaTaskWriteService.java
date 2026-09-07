package com.suture.crm.syna;

import com.suture.crm.audit.AuditTrailService;
import com.suture.crm.event.DomainEventService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class SynaTaskWriteService {
    private final JdbcClient jdbc;
    private final AuditTrailService audit;
    private final DomainEventService events;

    SynaTaskWriteService(JdbcClient jdbc, AuditTrailService audit, DomainEventService events) {
        this.jdbc = jdbc; this.audit = audit; this.events = events;
    }

    @Transactional
    public SynaTaskWriteResult create(SynaConnectionContext context, String idempotencyKey, CreateSynaTaskRequest request) {
        validate(idempotencyKey, request);
        Optional<SynaReadService.SynaTask> existing = findByKey(context.tenantId(), idempotencyKey);
        if (existing.isPresent()) return new SynaTaskWriteResult(existing.get(), false);

        Related related = related(context.tenantId(), request.relatedType(), request.relatedId());
        Optional<UUID> inserted = jdbc.sql("""
                INSERT INTO task (tenant_id, title, description, company_id, contact_id, opportunity_id, status,
                    priority, due_at, assigned_to, created_by, source, syna_idempotency_key)
                VALUES (:tenantId, :title, :description, :companyId, :contactId, :opportunityId, 'OPEN',
                    :priority, :dueAt, :assignedTo, :createdBy, 'AGENT', :idempotencyKey)
                ON CONFLICT (tenant_id, syna_idempotency_key) WHERE syna_idempotency_key IS NOT NULL DO NOTHING
                RETURNING id
                """).param("tenantId", context.tenantId()).param("title", request.title().trim())
                .param("description", blankToNull(request.description())).param("companyId", related.companyId())
                .param("contactId", related.contactId()).param("opportunityId", related.opportunityId())
                .param("priority", request.priority().trim().toUpperCase(Locale.ROOT)).param("dueAt", request.dueAt())
                .param("assignedTo", blankToNull(request.assignedTo())).param("createdBy", "syna:" + context.organizationId())
                .param("idempotencyKey", idempotencyKey).query(UUID.class).optional();
        if (inserted.isEmpty()) {
            return new SynaTaskWriteResult(findByKey(context.tenantId(), idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Idempotent task was not found")), false);
        }
        UUID id = inserted.get();
        SynaReadService.SynaTask task = find(id, context.tenantId());
        audit.recordAgentAction(context.tenantId(), "syna:" + context.organizationId(), "SYNA_TASK_CREATED", "task", id, task.title());
        events.publish(context.tenantId(), "TASK_CREATED", "task", id, task.title());
        return new SynaTaskWriteResult(task, true);
    }

    private Optional<SynaReadService.SynaTask> findByKey(UUID tenantId, String key) {
        return jdbc.sql(sql() + " AND t.syna_idempotency_key = :key").param("tenantId", tenantId).param("key", key)
                .query(this::task).optional();
    }
    private SynaReadService.SynaTask find(UUID id, UUID tenantId) {
        return jdbc.sql(sql() + " AND t.id = :id").param("tenantId", tenantId).param("id", id).query(this::task).optional()
                .orElseThrow(() -> new IllegalStateException("Created task was not found"));
    }
    private String sql() { return """
            SELECT t.id, t.title, t.status, t.assigned_to AS assignee, t.due_at,
                   CASE WHEN t.opportunity_id IS NOT NULL THEN 'deal' WHEN t.company_id IS NOT NULL THEN 'customer'
                        WHEN t.contact_id IS NOT NULL THEN 'contact' END AS related_type,
                   COALESCE(t.opportunity_id, t.company_id, t.contact_id) AS related_id, t.updated_at
              FROM task t WHERE t.tenant_id = :tenantId
            """; }
    private SynaReadService.SynaTask task(ResultSet rs, int ignored) throws SQLException {
        return new SynaReadService.SynaTask(rs.getObject("id", UUID.class), rs.getString("title"), rs.getString("status"),
                rs.getString("assignee"), rs.getObject("due_at", OffsetDateTime.class), rs.getString("related_type"),
                rs.getObject("related_id", UUID.class), rs.getObject("updated_at", OffsetDateTime.class));
    }
    private Related related(UUID tenantId, String type, UUID id) {
        String normalized = type.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "lead" -> jdbc.sql("SELECT company_id, contact_id, NULL::uuid AS opportunity_id FROM lead WHERE tenant_id = :tenantId AND id = :id")
                    .param("tenantId", tenantId).param("id", id).query(this::related).optional().orElseThrow(() -> missing("Lead"));
            case "deal" -> jdbc.sql("SELECT company_id, contact_id, id AS opportunity_id FROM opportunity WHERE tenant_id = :tenantId AND id = :id")
                    .param("tenantId", tenantId).param("id", id).query(this::related).optional().orElseThrow(() -> missing("Deal"));
            case "customer" -> jdbc.sql("SELECT id AS company_id, NULL::uuid AS contact_id, NULL::uuid AS opportunity_id FROM company WHERE tenant_id = :tenantId AND id = :id")
                    .param("tenantId", tenantId).param("id", id).query(this::related).optional().orElseThrow(() -> missing("Customer"));
            case "contact" -> jdbc.sql("SELECT company_id, id AS contact_id, NULL::uuid AS opportunity_id FROM contact WHERE tenant_id = :tenantId AND id = :id")
                    .param("tenantId", tenantId).param("id", id).query(this::related).optional().orElseThrow(() -> missing("Contact"));
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "relatedType is invalid");
        };
    }
    private Related related(ResultSet rs, int ignored) throws SQLException { return new Related(rs.getObject("company_id", UUID.class), rs.getObject("contact_id", UUID.class), rs.getObject("opportunity_id", UUID.class)); }
    private void validate(String key, CreateSynaTaskRequest request) {
        if (key == null || key.isBlank() || key.length() > 120) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid Idempotency-Key is required");
        if (!java.util.Set.of("LOW", "MEDIUM", "HIGH").contains(request.priority().trim().toUpperCase(Locale.ROOT)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "priority is invalid");
    }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private ResponseStatusException missing(String type) { return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " not found"); }

    private record Related(UUID companyId, UUID contactId, UUID opportunityId) { }
    public record CreateSynaTaskRequest(@NotBlank @Size(max = 240) String title, @Size(max = 4000) String description,
            @NotBlank String relatedType, @NotNull UUID relatedId, OffsetDateTime dueAt,
            @NotBlank String priority, @Size(max = 100) String assignedTo) { }
    public record SynaTaskWriteResult(SynaReadService.SynaTask task, boolean created) { }
}
