package com.suture.crm.dashboard;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final JdbcClient jdbc;
    DashboardService(JdbcClient jdbc) { this.jdbc = jdbc; }
    public DashboardResponse today(UUID tenantId) {
        return jdbc.sql("""
                WITH opportunity_summary AS (
                    SELECT COUNT(*) FILTER (WHERE next_action_date <= CURRENT_DATE AND stage NOT IN ('WON', 'LOST')) AS attention,
                           COUNT(*) FILTER (WHERE stage = 'PROPOSAL' AND next_action_date < CURRENT_DATE) AS waiting,
                           COALESCE(SUM(estimated_value) FILTER (WHERE stage NOT IN ('WON', 'LOST')), 0) AS pipeline
                      FROM opportunity WHERE tenant_id = :tenantId
                ), task_summary AS (
                    SELECT COUNT(*) AS overdue FROM task
                     WHERE tenant_id = :tenantId AND status = 'OPEN' AND due_at < now()
                )
                SELECT attention, overdue, waiting, pipeline FROM opportunity_summary CROSS JOIN task_summary
                """).param("tenantId", tenantId).query((rs, ignored) -> new DashboardResponse(
                rs.getLong("attention"), rs.getLong("overdue"), rs.getLong("waiting"), rs.getBigDecimal("pipeline"))).single();
    }
}
