-- A CRM connection needs independent least-privilege credentials for reads and writes.
-- V3 constrained an organization to one token, which prevented a READ_ONLY token and a
-- WRITE_TASKS token from coexisting.
ALTER TABLE syna_service_token
    DROP CONSTRAINT IF EXISTS syna_service_token_organization_id_key;

CREATE INDEX IF NOT EXISTS idx_syna_service_token_organization_active
    ON syna_service_token(organization_id)
    WHERE revoked_at IS NULL;
