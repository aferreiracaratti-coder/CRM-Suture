-- Suture CRM bootstrap SQL
--
-- Restore from the repository root with:
--   psql -d suture_crm -f database/suture-crm-bootstrap.sql
--
-- This is a reproducible logical bootstrap, not a pg_dump from a running
-- environment. It applies the versioned schema and the supplied prospecting
-- data in the same order as Flyway.
\set ON_ERROR_STOP on
\ir ../backend/src/main/resources/db/migration/V1__crm_mvp_schema.sql
\ir ../backend/src/main/resources/db/migration/V2__prospecting_data_and_company_channels.sql
\ir ../backend/src/main/resources/db/migration/V3__syna_service_tokens.sql
\ir ../backend/src/main/resources/db/migration/V4__syna_task_writes.sql
\ir ../backend/src/main/resources/db/migration/V5__crm_users.sql
\ir ../backend/src/main/resources/db/migration/V6__allow_separate_syna_write_tokens.sql
\ir ../backend/src/main/resources/db/migration/V7__multiuser_ownership.sql
