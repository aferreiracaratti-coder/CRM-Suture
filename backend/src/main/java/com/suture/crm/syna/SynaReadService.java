package com.suture.crm.syna;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SynaReadService {
    private final JdbcClient jdbc;

    SynaReadService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public SynaPage<SynaLead> leads(UUID tenantId, String query, int limit) {
        return new SynaPage<>(jdbc.sql(leadSql(false)).param("tenantId", tenantId)
                .param("query", query(query)).param("limit", limit(limit)).query(this::lead).list(), null);
    }

    public SynaLead lead(UUID tenantId, UUID id) {
        return jdbc.sql(leadSql(true)).param("tenantId", tenantId).param("id", id).query(this::lead).optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found"));
    }

    public SynaPage<SynaCustomer> customers(UUID tenantId, String query, int limit) {
        return new SynaPage<>(jdbc.sql(customerSql(false)).param("tenantId", tenantId)
                .param("query", query(query)).param("limit", limit(limit)).query(this::customer).list(), null);
    }

    public SynaCustomer customer(UUID tenantId, UUID id) {
        return jdbc.sql(customerSql(true)).param("tenantId", tenantId).param("id", id).query(this::customer).optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    public SynaPage<SynaTask> tasks(UUID tenantId, String query, String status, String assigneeId, OffsetDateTime dueBefore, int limit) {
        return new SynaPage<>(jdbc.sql(taskSql(false)).param("tenantId", tenantId).param("query", query(query))
                .param("status", optional(status)).param("assigneeId", optional(assigneeId)).param("dueBefore", dueBefore(dueBefore))
                .param("limit", limit(limit)).query(this::task).list(), null);
    }

    public SynaTask task(UUID tenantId, UUID id) {
        return jdbc.sql(taskSql(true)).param("tenantId", tenantId).param("id", id).query(this::task).optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    public SynaPage<SynaDeal> deals(UUID tenantId, String query, String stage, int limit) {
        return new SynaPage<>(jdbc.sql(dealSql(false)).param("tenantId", tenantId).param("query", query(query))
                .param("stage", optional(stage)).param("limit", limit(limit)).query(this::deal).list(), null);
    }

    public SynaDeal deal(UUID tenantId, UUID id) {
        return jdbc.sql(dealSql(true)).param("tenantId", tenantId).param("id", id).query(this::deal).optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deal not found"));
    }

    private String leadSql(boolean detail) {
        return """
                SELECT l.id, COALESCE(NULLIF(trim(concat_ws(' ', ct.first_name, ct.last_name)), ''), 'Lead ' || left(l.id::text, 8)) AS name,
                       co.name AS company, l.status, l.owner_id::text AS owner, l.next_contact_at AS next_action_at,
                       l.created_at, l.updated_at
                  FROM lead l JOIN company co ON co.id = l.company_id
             LEFT JOIN contact ct ON ct.id = l.contact_id
                 WHERE l.tenant_id = :tenantId
                """ + (detail ? "AND l.id = :id" : """
                AND (:query = '' OR lower(concat_ws(' ', ct.first_name, ct.last_name, co.name, l.status)) LIKE :query)
                 ORDER BY l.updated_at DESC
                 LIMIT :limit
                """);
    }

    private String customerSql(boolean detail) {
        return """
                SELECT c.id, c.name, c.name AS company, c.status AS lifecycle, c.owner_id::text AS owner, c.updated_at
                  FROM company c
                 WHERE c.tenant_id = :tenantId
                """ + (detail ? "AND c.id = :id" : """
                AND (:query = '' OR lower(concat_ws(' ', c.name, c.status, c.industry, c.city)) LIKE :query)
                 ORDER BY c.name
                 LIMIT :limit
                """);
    }

    private String taskSql(boolean detail) {
        return """
                SELECT t.id, t.title, t.status, COALESCE(t.assignee_id::text, t.assigned_to) AS assignee, t.due_at,
                       CASE WHEN t.opportunity_id IS NOT NULL THEN 'deal'
                            WHEN t.company_id IS NOT NULL THEN 'customer'
                            WHEN t.contact_id IS NOT NULL THEN 'contact' END AS related_type,
                       COALESCE(t.opportunity_id, t.company_id, t.contact_id) AS related_id, t.updated_at
                  FROM task t
                 WHERE t.tenant_id = :tenantId
                """ + (detail ? "AND t.id = :id" : """
                AND (:query = '' OR lower(t.title) LIKE :query)
                AND (:status = '' OR t.status = :status)
                AND (:assigneeId = '' OR t.assigned_to = :assigneeId)
                AND (t.due_at IS NULL OR t.due_at <= :dueBefore)
                 ORDER BY t.due_at NULLS LAST, t.updated_at DESC
                 LIMIT :limit
                """);
    }

    private String dealSql(boolean detail) {
        return """
                SELECT o.id, o.name, c.name AS company, o.stage, o.estimated_value AS amount, o.currency,
                       o.owner_id::text AS owner, o.expected_close_date AS close_date,
                       o.next_action_date::timestamptz AS next_action_at, o.created_at, o.updated_at
                  FROM opportunity o JOIN company c ON c.id = o.company_id
                 WHERE o.tenant_id = :tenantId
                """ + (detail ? "AND o.id = :id" : """
                AND (:query = '' OR lower(concat_ws(' ', o.name, c.name, o.stage)) LIKE :query)
                AND (:stage = '' OR o.stage = :stage)
                 ORDER BY o.next_action_date NULLS LAST, o.updated_at DESC
                 LIMIT :limit
                """);
    }

    private SynaLead lead(ResultSet rs, int ignored) throws SQLException {
        return new SynaLead(uuid(rs, "id"), rs.getString("name"), rs.getString("company"), rs.getString("status"),
                rs.getString("owner"), offsetDateTime(rs, "next_action_at"), offsetDateTime(rs, "created_at"), offsetDateTime(rs, "updated_at"));
    }

    private SynaCustomer customer(ResultSet rs, int ignored) throws SQLException {
        return new SynaCustomer(uuid(rs, "id"), rs.getString("name"), rs.getString("company"), rs.getString("lifecycle"),
                rs.getString("owner"), offsetDateTime(rs, "updated_at"));
    }

    private SynaTask task(ResultSet rs, int ignored) throws SQLException {
        return new SynaTask(uuid(rs, "id"), rs.getString("title"), rs.getString("status"), rs.getString("assignee"),
                offsetDateTime(rs, "due_at"), rs.getString("related_type"), uuid(rs, "related_id"), offsetDateTime(rs, "updated_at"));
    }

    private SynaDeal deal(ResultSet rs, int ignored) throws SQLException {
        return new SynaDeal(uuid(rs, "id"), rs.getString("name"), rs.getString("company"), rs.getString("stage"),
                rs.getObject("amount", BigDecimal.class), rs.getString("currency"), rs.getString("owner"),
                rs.getObject("close_date", LocalDate.class), offsetDateTime(rs, "next_action_at"), offsetDateTime(rs, "created_at"), offsetDateTime(rs, "updated_at"));
    }

    private UUID uuid(ResultSet rs, String column) throws SQLException { return rs.getObject(column, UUID.class); }
    private OffsetDateTime offsetDateTime(ResultSet rs, String column) throws SQLException { return rs.getObject(column, OffsetDateTime.class); }
    private String query(String value) { return value == null || value.isBlank() ? "" : "%" + value.trim().toLowerCase() + "%"; }
    private String optional(String value) { return value == null ? "" : value.trim(); }
    private OffsetDateTime dueBefore(OffsetDateTime value) { return value == null ? OffsetDateTime.parse("9999-12-31T23:59:59Z") : value; }
    private int limit(int value) { return Math.max(1, Math.min(50, value)); }

    public record SynaPage<T>(List<T> items, String nextCursor) { }
    public record SynaLead(UUID id, String name, String company, String status, String owner, OffsetDateTime nextActionAt, OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
    public record SynaCustomer(UUID id, String name, String company, String lifecycle, String owner, OffsetDateTime updatedAt) { }
    public record SynaTask(UUID id, String title, String status, String assignee, OffsetDateTime dueAt, String relatedType, UUID relatedId, OffsetDateTime updatedAt) { }
    public record SynaDeal(UUID id, String name, String company, String stage, BigDecimal amount, String currency, String owner, LocalDate closeDate, OffsetDateTime nextActionAt, OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
}
