# Base de datos de Suture CRM

El esquema y los datos iniciales son versionados por Flyway en
`backend/src/main/resources/db/migration/`:

- `V1__crm_mvp_schema.sql`: modelo CRM inicial.
- `V2__prospecting_data_and_company_channels.sql`: canales de empresa y datos
  de prospección aportados para el MVP.

## Restauración local

Con PostgreSQL instalado y una base vacía llamada `suture_crm`:

```powershell
psql -d suture_crm -f database/suture-crm-bootstrap.sql
```

El archivo `suture-crm-bootstrap.sql` es un bootstrap lógico reproducible que
incluye ambas migraciones mediante `psql`. No se generó un `pg_dump` de una
instancia viva porque este entorno todavía no tiene PostgreSQL ejecutándose.
Cuando la base esté desplegada en Linux, se podrá añadir un dump operativo
sanitizado como respaldo independiente.
