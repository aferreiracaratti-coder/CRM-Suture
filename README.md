# Suture CRM

Monorepo inicial para el CRM comercial y su futura capa de agentes.

- `frontend/`: React + Vite; experiencia operativa del MVP.
- `backend/`: monolito modular Spring Boot, Flyway y API inicial de empresas/dashboard.
- `database/`: bootstrap SQL reproducible de esquema y datos iniciales.
- `docs/roadmap.md`: fases y criterios de salida.
- `docs/design/`: concepto visual de referencia.

## Arranque del frontend

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

La API, PostgreSQL y OpenAI no forman parte de esta primera pantalla: la interfaz simula información operacional para validar el flujo antes de persistir datos.

## Backend y base

El backend queda preparado para PostgreSQL y no toca ninguna base actual. Durante esta etapa se puede avanzar en módulos, contratos y tests sin una base local corriendo.

Cuando el entorno pase a Linux, `compose.yaml` levantará una base aislada llamada `suture_crm` y un volumen propio; recién entonces se ejecutará la API contra PostgreSQL y Flyway aplicará la migración inicial.

Para cargar el esquema y los datos iniciales manualmente con PostgreSQL:

```powershell
psql -d suture_crm -f database/suture-crm-bootstrap.sql
```

Ver [database/README.md](database/README.md) para el alcance del bootstrap y la futura generación de un respaldo operativo.
