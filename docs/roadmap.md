# Suture CRM — roadmap por fases

## Fase 1 · MVP usable

Objetivo: responder todos los días **qué oportunidades tengo y qué debería hacer hoy**, sin IA.

- Empresas y contactos, con `tenant_id` desde el inicio.
- Leads separados de oportunidades.
- Pipeline: `NEW`, `CONTACTED`, `DISCOVERY`, `QUALIFIED`, `PROPOSAL`, `NEGOTIATION`, `WON`, `LOST`.
- Interacciones, tareas y próximos pasos.
- Dashboard de prioridades, seguimientos vencidos y pipeline abierto.

**Criterio de salida:** se puede gestionar una oportunidad completa y registrar cada interacción sin salir de Suture.

## Fase 2 · Agent-ready (sin modelo)

Objetivo: preparar el dominio para que una persona, una automatización o un agente usen las mismas capacidades.

- `Actor` (`USER`, `AGENT`, `SYSTEM`) y auditoría.
- Servicios de dominio: crear tarea, registrar interacción, mover etapa.
- `domain_event` persistido en PostgreSQL; aún sin Kafka.
- Interfaces de tools y registro de capacidades, sin conectar IA.

**Criterio de salida:** toda mutación comercial se audita y emite un evento.

## Fase 3 · Agente de lectura

Objetivo: contestar “¿qué debería hacer hoy?” con datos reales.

- Conversaciones, mensajes, ejecución de tools y uso/costo de IA.
- Tools de solo lectura: pipeline, vencidos, interacciones recientes, búsqueda de empresas y oportunidades estancadas.
- Panel contextual: la pantalla actual viaja como contexto al agente.

**Criterio de salida:** el agente prioriza desde datos reales sin inventar ni modificar datos.

## Fase 4 · Escritura segura

Objetivo: convertir recomendaciones en trabajo real, con control humano.

- Crear tareas, notas y follow-ups como acciones permitidas.
- Borradores antes de enviar o cambiar información sensible.
- Aprobaciones para acciones críticas; nunca SQL directo del agente.

## Fase 5 · Proactividad

Objetivo: detectar oportunidades de acción antes de que se pierdan.

- Eventos como `FOLLOW_UP_OVERDUE` disparan una recomendación.
- Memoria estructurada por empresa, no historiales gigantes de chat.
- Métricas de adopción, auditoría y costo por tenant/agente.

## Regla arquitectónica permanente

El agente consume herramientas del dominio, por ejemplo `getPipelineSummary`, `createTask` y `addInteraction`. Nunca accede a SQL ni salta las mismas reglas y auditoría que usa la aplicación.
