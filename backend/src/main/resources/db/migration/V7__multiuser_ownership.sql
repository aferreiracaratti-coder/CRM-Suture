-- Suture CRM multiusuario: dueños, asignaciones y autores con FK real a crm_user.
-- Se mantienen las columnas legacy asignadas/created_by de tipo texto para no romper Syna,
-- pero las nuevas referencias permiten aplicar permisos y reportes por usuario.

ALTER TABLE company
    ADD COLUMN owner_id UUID REFERENCES crm_user(id),
    ADD COLUMN created_by_id UUID REFERENCES crm_user(id),
    ADD COLUMN updated_by_id UUID REFERENCES crm_user(id);

ALTER TABLE lead
    ADD COLUMN owner_id UUID REFERENCES crm_user(id),
    ADD COLUMN created_by_id UUID REFERENCES crm_user(id),
    ADD COLUMN updated_by_id UUID REFERENCES crm_user(id);

ALTER TABLE opportunity
    ADD COLUMN owner_id UUID REFERENCES crm_user(id),
    ADD COLUMN created_by_id UUID REFERENCES crm_user(id),
    ADD COLUMN updated_by_id UUID REFERENCES crm_user(id);

ALTER TABLE contact
    ADD COLUMN created_by_id UUID REFERENCES crm_user(id),
    ADD COLUMN updated_by_id UUID REFERENCES crm_user(id);

ALTER TABLE task
    ADD COLUMN assignee_id UUID REFERENCES crm_user(id),
    ADD COLUMN created_by_id UUID REFERENCES crm_user(id),
    ADD COLUMN updated_by_id UUID REFERENCES crm_user(id);

ALTER TABLE interaction
    ADD COLUMN created_by_id UUID REFERENCES crm_user(id);

CREATE INDEX idx_company_owner ON company(tenant_id, owner_id);
CREATE INDEX idx_lead_owner ON lead(tenant_id, owner_id);
CREATE INDEX idx_opportunity_owner ON opportunity(tenant_id, owner_id);
CREATE INDEX idx_task_assignee ON task(tenant_id, assignee_id) WHERE status = 'OPEN';
