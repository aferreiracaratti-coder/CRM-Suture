# Base de datos de Suture CRM

El esquema y los datos iniciales son versionados por Flyway en
`backend/src/main/resources/db/migration/`:

- `V1__crm_mvp_schema.sql`: modelo CRM inicial.
- `V2__prospecting_data_and_company_channels.sql`: canales de empresa y datos
  de prospección aportados para el MVP.
- `V3__syna_service_tokens.sql`: conexiones de servicio de Syna.
- `V4__syna_task_writes.sql`: escritura idempotente de tareas desde Syna.
- `V5__crm_users.sql`: usuarios del CRM.
- `V6__allow_separate_syna_write_tokens.sql`: tokens independientes de lectura
  y escritura.
- `V7__multiuser_ownership.sql`: dueños, asignaciones y autores por usuario.

## Restauración local

Con PostgreSQL instalado y una base vacía llamada `suture_crm`:

```powershell
psql -d suture_crm -f database/suture-crm-bootstrap.sql
```

El archivo `suture-crm-bootstrap.sql` es un bootstrap lógico reproducible que
incluye las migraciones `V1` a `V7` mediante `psql`. No se generó un `pg_dump` de una
instancia viva porque este entorno todavía no tiene PostgreSQL ejecutándose.
Cuando la base esté desplegada en Linux, se podrá añadir un dump operativo
sanitizado como respaldo independiente.
