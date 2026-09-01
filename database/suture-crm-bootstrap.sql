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
