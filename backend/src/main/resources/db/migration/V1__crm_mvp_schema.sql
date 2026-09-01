CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE tenant (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    slug VARCHAR(80) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE company (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    name VARCHAR(200) NOT NULL,
    website VARCHAR(300),
    industry VARCHAR(120),
    city VARCHAR(120),
    country VARCHAR(120),
    source VARCHAR(80),
    status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, name)
);
CREATE INDEX idx_company_tenant_name ON company(tenant_id, name);

CREATE TABLE contact (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    company_id UUID NOT NULL REFERENCES company(id),
    first_name VARCHAR(120) NOT NULL,
    last_name VARCHAR(120),
    role VARCHAR(160),
    email VARCHAR(250),
    phone VARCHAR(80),
    instagram VARCHAR(160),
    linkedin VARCHAR(300),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_contact_tenant_company ON contact(tenant_id, company_id);

CREATE TABLE lead (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    company_id UUID NOT NULL REFERENCES company(id),
    contact_id UUID REFERENCES contact(id),
    source VARCHAR(80),
    temperature VARCHAR(10) NOT NULL DEFAULT 'COLD' CHECK (temperature IN ('COLD','WARM','HOT')),
    status VARCHAR(40) NOT NULL DEFAULT 'NEW',
    first_contact_at TIMESTAMPTZ,
    last_contact_at TIMESTAMPTZ,
    next_contact_at TIMESTAMPTZ,
    score INTEGER NOT NULL DEFAULT 0 CHECK (score BETWEEN 0 AND 100),
    summary TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_lead_tenant_status ON lead(tenant_id, status);

CREATE TABLE opportunity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    company_id UUID NOT NULL REFERENCES company(id),
    contact_id UUID REFERENCES contact(id),
    name VARCHAR(240) NOT NULL,
    stage VARCHAR(20) NOT NULL DEFAULT 'NEW' CHECK (stage IN ('NEW','CONTACTED','DISCOVERY','QUALIFIED','PROPOSAL','NEGOTIATION','WON','LOST')),
    estimated_value NUMERIC(14,2),
    currency CHAR(3) NOT NULL DEFAULT 'UYU',
    probability SMALLINT CHECK (probability BETWEEN 0 AND 100),
    expected_close_date DATE,
    next_action TEXT,
    next_action_date DATE,
    lost_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_opportunity_tenant_stage ON opportunity(tenant_id, stage);
CREATE INDEX idx_opportunity_next_action ON opportunity(tenant_id, next_action_date) WHERE stage NOT IN ('WON','LOST');

CREATE TABLE interaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    company_id UUID NOT NULL REFERENCES company(id),
    contact_id UUID REFERENCES contact(id),
    opportunity_id UUID REFERENCES opportunity(id),
    type VARCHAR(20) NOT NULL CHECK (type IN ('MESSAGE','CALL','EMAIL','MEETING','NOTE','VISIT')),
    channel VARCHAR(20) CHECK (channel IN ('WHATSAPP','INSTAGRAM','EMAIL','PHONE','IN_PERSON','OTHER')),
    direction VARCHAR(10) CHECK (direction IN ('INBOUND','OUTBOUND','INTERNAL')),
    subject VARCHAR(250),
    content TEXT,
    summary TEXT,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_interaction_company_recent ON interaction(tenant_id, company_id, occurred_at DESC);

CREATE TABLE task (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    title VARCHAR(240) NOT NULL,
    description TEXT,
    company_id UUID REFERENCES company(id),
    contact_id UUID REFERENCES contact(id),
    opportunity_id UUID REFERENCES opportunity(id),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','COMPLETED','CANCELLED')),
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW','MEDIUM','HIGH')),
    due_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    assigned_to VARCHAR(100),
    created_by VARCHAR(100) NOT NULL,
    source VARCHAR(20) NOT NULL DEFAULT 'HUMAN' CHECK (source IN ('HUMAN','AGENT','AUTOMATION')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_task_tenant_due ON task(tenant_id, due_at) WHERE status = 'OPEN';

CREATE TABLE domain_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    type VARCHAR(80) NOT NULL,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id UUID NOT NULL,
    payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ
);
CREATE INDEX idx_domain_event_unprocessed ON domain_event(created_at) WHERE processed_at IS NULL;

CREATE TABLE audit_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    actor_type VARCHAR(20) NOT NULL CHECK (actor_type IN ('USER','AGENT','SYSTEM')),
    actor_id VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id UUID NOT NULL,
    data JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO tenant (id, name, slug)
VALUES ('00000000-0000-0000-0000-000000000001', 'Suture Sistemas', 'suture')
ON CONFLICT (id) DO NOTHING;
