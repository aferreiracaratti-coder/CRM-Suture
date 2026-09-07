# Backend: monolito modular

La primera implementación será un único Spring Boot con PostgreSQL. Se mantiene la separación lógica sin convertir cada módulo en un proceso independiente.

```text
src/main/java/com/suture/crm/
├── auth/
├── tenant/
├── crm/
│   ├── company/
│   ├── contact/
│   ├── lead/
│   ├── opportunity/
│   ├── interaction/
│   └── pipeline/
├── task/
├── event/
├── audit/
├── agent/               # se habilita en fase 3
│   ├── conversation/
│   ├── tools/
│   ├── execution/
│   ├── approval/
│   └── cost/
└── common/
```

## Convenciones que quedan fijadas desde Fase 1

- Todas las entidades comerciales incluyen `tenant_id`.
- El tenant se deriva de la sesión autenticada; no se acepta `X-Tenant-Id`
  como fuente de confianza.
- Los cambios pasan por servicios de dominio; los controladores no escriben repositorios directamente.
- Las acciones aceptan un `Actor` (`USER`, `AGENT`, `SYSTEM`) y generan auditoría/eventos.
- El futuro agente usa el mismo `Tool / Action Layer` que la UI. No accede a SQL.

No se incluye todavía una dependencia de OpenAI ni una clave de API. Eso comienza en Fase 3 después de contar con datos reales y tools de lectura verificables.

## Base y arranque

La API queda lista para PostgreSQL, pero su desarrollo de estructura y contratos no depende de tener Docker en Windows. La base se levantará al migrar el entorno a Linux, desde la raíz:

```bash
docker compose up -d postgres
cd backend
./mvnw spring-boot:run
```

La primera ejecución aplica las migraciones `V1` a `V7` y crea el tenant inicial
`Suture Sistemas` con ID `00000000-0000-0000-0000-000000000001`.

## Verticales disponibles

- `GET /api/health`
- `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout`.
- `GET /api/users` y mutaciones de usuarios, sólo para `CRM_ADMIN`.
- `GET /api/companies` y `POST /api/companies`.
- `GET /api/leads`, `/api/customers`, `/api/tasks` y `/api/deals` para Syna.
- `GET /api/contacts?companyId={id}` y `POST /api/contacts`.
- `GET /api/opportunities`, `POST /api/opportunities` y `PATCH /api/opportunities/{id}/stage`.
- `GET /api/dashboard/today`.

Crear empresas, contactos u oportunidades persiste un evento de dominio y una entrada de auditoría. Los cambios de etapa de oportunidad siguen el mismo recorrido por servicios de dominio.
