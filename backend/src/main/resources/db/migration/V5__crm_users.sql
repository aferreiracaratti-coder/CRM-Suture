CREATE TABLE crm_user (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    email VARCHAR(320) NOT NULL,
    display_name VARCHAR(160) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    roles VARCHAR(500) NOT NULL DEFAULT 'CRM_USER,SYNA_SALES',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT crm_user_tenant_email_unique UNIQUE (tenant_id, email)
);

CREATE INDEX crm_user_email_active_idx ON crm_user (email) WHERE active = TRUE;
