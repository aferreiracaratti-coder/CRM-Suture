CREATE TABLE syna_service_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL UNIQUE,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    token_hash CHAR(64) NOT NULL UNIQUE,
    permissions VARCHAR(20) NOT NULL DEFAULT 'READ_ONLY' CHECK (permissions = 'READ_ONLY'),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at TIMESTAMPTZ
);

CREATE INDEX idx_syna_service_token_active
    ON syna_service_token(token_hash)
    WHERE revoked_at IS NULL;
