ALTER TABLE syna_service_token
    DROP CONSTRAINT IF EXISTS syna_service_token_permissions_check;

ALTER TABLE syna_service_token
    ADD CONSTRAINT syna_service_token_permissions_check
    CHECK (permissions IN ('READ_ONLY', 'WRITE_TASKS'));

ALTER TABLE task
    ADD COLUMN IF NOT EXISTS syna_idempotency_key VARCHAR(120);

CREATE UNIQUE INDEX IF NOT EXISTS idx_task_syna_idempotency
    ON task(tenant_id, syna_idempotency_key)
    WHERE syna_idempotency_key IS NOT NULL;
