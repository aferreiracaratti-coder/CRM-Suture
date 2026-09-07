package com.suture.crm.crm.task;

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
public class CrmTaskService {
    private final JdbcClient jdbc;
    private final CompanyRepository companies;
    private final AuditTrailService audit;
    private final DomainEventService events;

    CrmTaskService(JdbcClient jdbc, CompanyRepository companies, AuditTrailService audit, DomainEventService events) {
        this.jdbc = jdbc;
        this.companies = companies;
        this.audit = audit;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<CrmTaskResponse> list(UUID tenantId) {
        return jdbc.sql("""
                SELECT t.id, t.title, t.description, t.company_id, t.status, t.priority,
                       t.due_at, t.completed_at, t.assigned_to, t.source, t.created_at, t.updated_at
                  FROM task t
                 WHERE t.tenant_id = :tenantId
                 ORDER BY t.status = 'OPEN' DESC, t.due_at NULLS LAST, t.created_at DESC
                """).param("tenantId", tenantId).query(this::map).list();
    }

    @Transactional
    public CrmTaskResponse create(UUID tenantId, UUID actorId, CreateCrmTaskRequest request) {
        if (request.companyId() == null || !companies.existsByIdAndTenantId(request.companyId(), tenantId)) {
            throw new ResourceNotFoundException("Empresa no encontrada");
        }
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO task (id, tenant_id, title, description, company_id, status, priority,
                    due_at, assigned_to, created_by, source, assignee_id, created_by_id)
                VALUES (:id, :tenantId, :title, :description, :companyId, 'OPEN', :priority,
                    :dueAt, :assignedTo, :createdBy, 'HUMAN', NULL, :createdBy)
                """)
                .param("id", id).param("tenantId", tenantId).param("title", request.title().trim())
                .param("description", blankToNull(request.description())).param("companyId", request.companyId())
                .param("priority", normalizePriority(request.priority())).param("dueAt", request.dueAt())
                .param("assignedTo", request.assigneeId() == null ? null : request.assigneeId().toString())
                .param("createdBy", actorId.toString())
                .update();
        events.publish(tenantId, "TASK_CREATED", "TASK", id, request.title());
        audit.recordUserAction(tenantId, actorId, "TASK_CREATED", "TASK", id, request.title());
        return find(tenantId, id);
    }

    @Transactional
    public CrmTaskResponse toggle(UUID tenantId, UUID actorId, UUID id, boolean done) {
        CrmTaskResponse existing = find(tenantId, id);
        jdbc.sql("""
                UPDATE task
                   SET status = :status, completed_at = :completedAt, updated_at = now()
                 WHERE tenant_id = :tenantId AND id = :id
                """)
                .param("status", done ? "COMPLETED" : "OPEN")
                .param("completedAt", done ? OffsetDateTime.now() : null)
                .param("tenantId", tenantId).param("id", id).update();
        audit.recordUserAction(tenantId, actorId, done ? "TASK_COMPLETED" : "TASK_REOPENED", "TASK", id, existing.title());
        return find(tenantId, id);
    }

    @Transactional
    public void delete(UUID tenantId, UUID actorId, UUID id) {
        CrmTaskResponse existing = find(tenantId, id);
        jdbc.sql("DELETE FROM task WHERE tenant_id = :tenantId AND id = :id")
                .param("tenantId", tenantId).param("id", id).update();
        events.publish(tenantId, "TASK_DELETED", "TASK", id, existing.title());
        audit.recordUserAction(tenantId, actorId, "TASK_DELETED", "TASK", id, existing.title());
    }

    private CrmTaskResponse find(UUID tenantId, UUID id) {
        return jdbc.sql("""
                SELECT t.id, t.title, t.description, t.company_id, t.status, t.priority,
                       t.due_at, t.completed_at, t.assigned_to, t.source, t.created_at, t.updated_at
                  FROM task t
                 WHERE t.tenant_id = :tenantId AND t.id = :id
                """).param("tenantId", tenantId).param("id", id).query(this::map).optional()
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));
    }

    private CrmTaskResponse map(ResultSet rs, int ignored) throws SQLException {
        String status = rs.getString("status");
        return new CrmTaskResponse(
                rs.getObject("id", UUID.class),
                rs.getString("title"),
                rs.getString("description"),
                rs.getObject("company_id", UUID.class),
                status,
                rs.getString("priority"),
                rs.getObject("due_at", OffsetDateTime.class),
                rs.getObject("completed_at", OffsetDateTime.class),
                rs.getString("assigned_to"),
                rs.getString("source"),
                "COMPLETED".equals(status),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getObject("updated_at", OffsetDateTime.class));
    }

    private String normalizePriority(String value) {
        String normalized = value == null || value.isBlank() ? "MEDIUM" : value.trim().toUpperCase(Locale.ROOT);
        return java.util.Set.of("LOW", "MEDIUM", "HIGH").contains(normalized) ? normalized : "MEDIUM";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record CreateCrmTaskRequest(UUID companyId, String title, String description, OffsetDateTime dueAt, String priority, UUID assigneeId) { }
    public record CrmTaskResponse(UUID id, String title, String description, UUID companyId, String status,
                                  String priority, OffsetDateTime dueAt, OffsetDateTime completedAt,
                                  String assignedTo, String source, boolean done, OffsetDateTime createdAt,
                                  OffsetDateTime updatedAt) { }
}
