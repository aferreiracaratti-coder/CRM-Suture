# Integración CRM con Syna

## Alcance

La primera integración es estrictamente de lectura: Syna puede buscar y consultar el
detalle de leads, clientes, tareas y oportunidades. No crea, actualiza ni elimina datos
en el CRM. Cada llamada se resuelve con la conexión de la organización activa de la
conversación.

Syna nunca lee la base de datos del CRM. El CRM continúa siendo la fuente de verdad. Los
resultados se registran como llamadas a herramientas con fecha y procedencia, pero no se
transforman automáticamente en memoria durable.

## Contrato del CRM

El CRM debe exponer una API HTTPS equivalente a:

```http
GET /api/leads?query={text}&limit={1..50}
GET /api/leads/{leadId}
GET /api/customers?query={text}&limit={1..50}
GET /api/customers/{customerId}
GET /api/tasks?query={text}&status={optional}&assigneeId={optional}&dueBefore={ISO-8601 optional}&limit={1..50}
GET /api/tasks/{taskId}
GET /api/deals?query={text}&stage={optional}&limit={1..50}
GET /api/deals/{dealId}
```

Debe requerir `Authorization: Bearer <token>` y recibir `X-Syna-Organization-Id`. La
organización efectiva debe salir del token de servicio y el CRM debe rechazar con `403`
cualquier cabecera cuyo tenant no coincida. La cabecera enviada por Syna es una validación
cruzada y de auditoría, nunca la única barrera.

Una búsqueda puede devolver una lista o un objeto contenedor. Usá el mismo sobre
`{ "items": [], "nextCursor": null }` para todas las colecciones. Cada recurso debe
incluir un identificador estable y `updatedAt`. Los campos mínimos recomendados son:

| Recurso | Campos para Syna |
| --- | --- |
| Lead | `id`, `name`, `company`, `status`, `owner`, `nextActionAt`, `updatedAt` |
| Cliente | `id`, `name`, `company`, `lifecycle`, `owner`, `updatedAt` |
| Tarea | `id`, `title`, `status`, `assignee`, `dueAt`, `relatedType`, `relatedId`, `updatedAt` |
| Oportunidad | `id`, `name`, `company`, `stage`, `amount`, `currency`, `owner`, `closeDate`, `updatedAt` |

```json
{
  "items": [{
    "id": "lead_123",
    "name": "Nahuel Pérez",
    "company": "Suture",
    "status": "qualified",
    "owner": "sales_1",
    "updatedAt": "2026-09-01T14:00:00Z"
  }],
  "nextCursor": null
}
```

Usá `401` para token inválido, `403` para tenant incorrecto, `404` si el lead no existe,
`429` para rate limit y `5xx` para errores del CRM. Nunca entregues al conector credenciales,
secretos, tarjetas completas ni notas restringidas.

## Configuración de Syna

1. Creá en el CRM un token de servicio limitado a lectura y a una única organización.
2. Guardalo en el entorno o gestor de secretos de Syna. En local, por ejemplo:

   ```dotenv
   CRM_ENABLED=true
   CRM_SUTURE_TOKEN=valor-del-token
   ```

3. Guardá sólo la referencia al secreto en PostgreSQL, nunca el token:

   ```sql
   INSERT INTO integration_connections (
     organization_id, app_key, display_name, base_url,
     auth_type, credential_secret_ref, enabled, metadata
   ) VALUES (
     'ORGANIZATION_UUID', 'crm', 'CRM de Suture', 'https://crm.example.com',
     'BEARER', 'env:CRM_SUTURE_TOKEN', true,
     '{
       "leadSearchPath":"/api/leads",
       "leadDetailPath":"/api/leads/{id}",
       "customerSearchPath":"/api/customers",
       "customerDetailPath":"/api/customers/{id}",
       "taskSearchPath":"/api/tasks",
       "taskDetailPath":"/api/tasks/{id}",
       "dealSearchPath":"/api/deals",
       "dealDetailPath":"/api/deals/{id}",
       "writeCredentialSecretRef":"env:CRM_SUTURE_WRITE_TOKEN"
     }'::jsonb
   )
   ON CONFLICT (organization_id, app_key) DO UPDATE SET
     display_name = EXCLUDED.display_name,
     base_url = EXCLUDED.base_url,
     auth_type = EXCLUDED.auth_type,
     credential_secret_ref = EXCLUDED.credential_secret_ref,
     enabled = EXCLUDED.enabled,
     metadata = EXCLUDED.metadata,
     updated_at = now();
   ```

   Los nombres históricos `searchPath` y `detailPath` siguen funcionando únicamente como
   alias de los paths de leads, para no romper la primera configuración.

4. Reiniciá Syna. Cuando `CRM_ENABLED=true`, las herramientas de lectura quedan disponibles
en los modos General, Ventas, Marketing y Analytics; las tareas también están disponibles
en Operaciones.

## Prueba funcional

Dentro de una conversación de la organización configurada, pedile a Syna:

> Buscá el lead de Nahuel en el CRM y decime su estado.

> ¿Qué tareas abiertas vencen esta semana y quién las tiene asignadas?

> Mostrame las oportunidades de Suture que están en negociación.

Syna ejecutará una de estas herramientas con el `organizationId` de la conversación:

```text
search_crm_leads       get_crm_lead
search_crm_customers   get_crm_customer
search_crm_tasks       get_crm_task
search_crm_deals       get_crm_deal
```

Repetí la consulta desde otra organización sin una conexión CRM: debe responder que no
hay una conexión CRM activa, sin realizar la llamada ni filtrar datos del primer tenant.

## Cómo manejar los datos: vivo, memoria y eventos

El CRM es la fuente de verdad de estado cambiante: pipeline, clientes, tareas, importes y
asignaciones se consultan **en vivo** mediante estas herramientas. Syna guarda la llamada
auditada, pero no indexa automáticamente toda la ficha ni cada nota como memoria.

La memoria durable queda para información estable y útil entre turnos, por ejemplo una
decisión comercial confirmada, una preferencia del cliente o un objetivo. Si Syna conserva
algo derivado del CRM debe indicar procedencia `TOOL_VERIFIED`, referencia al recurso y su
fecha; una respuesta del modelo no se convierte por sí sola en verdad.

Para sincronización proactiva, la siguiente fase recomendada es un webhook *outbox* del CRM:
el CRM emite sólo `{eventId, organizationId, resourceType, resourceId, occurredAt, version}`
firmado. Syna valida firma, deduplica `eventId` y vuelve a pedir el recurso al CRM con su
token de tenant. No envíes el registro entero ni secretos por el webhook. Implementalo una
vez que el CRM tenga firma HMAC/JWT de servicio, reintentos e idempotencia; no se habilitó un
webhook abierto e inseguro.

## Creación segura de tareas

Las tareas no se crean con una confirmación escrita en el chat. El flujo completo es:

```text
Syna diagnostica datos vivos -> propone la tarea -> usuario solicita crearla
-> Syna devuelve pendingActions[] -> usuario toca Aprobar en CRM
-> CRM BFF llama POST /v1/actions/{id}/approve con su JWT
-> Syna ejecuta POST /api/tasks con Idempotency-Key -> CRM audita y emite evento
```

El CRM expone `POST /api/tasks` exclusivamente para Syna. Requiere `Authorization: Bearer`,
`X-Syna-Organization-Id` e `Idempotency-Key`; el token debe tener permiso `WRITE_TASKS`.
El body admitido es:

```json
{
  "title": "Contactar a Estudio Manzor",
  "description": "Confirmar propuesta y próximo paso comercial.",
  "relatedType": "deal",
  "relatedId": "UUID",
  "dueAt": "2026-09-03T14:00:00-03:00",
  "priority": "HIGH",
  "assignedTo": "sales_1"
}
```

`relatedType` puede ser `lead`, `deal`, `customer` o `contact`. El CRM resuelve la relación
desde el tenant del token, por lo que no acepta referencias de otra empresa. La misma
`Idempotency-Key` devuelve la tarea ya creada en lugar de duplicarla. Cada alta queda con
`source=AGENT`, auditoría `SYNA_TASK_CREATED` y evento `TASK_CREATED`.

Para habilitarlo, rotá a un token de servicio separado para escritura y actualizá su permiso:

```sql
UPDATE syna_service_token
   SET permissions = 'WRITE_TASKS'
 WHERE organization_id = 'ORGANIZATION_UUID';
```

Usá `CRM_SUTURE_TOKEN` con permiso `READ_ONLY` y `CRM_SUTURE_WRITE_TOKEN` con
`WRITE_TASKS`. El conector usa el segundo únicamente para `POST /api/tasks`; los valores
nunca se guardan en PostgreSQL de Syna, sólo sus referencias `env:`.

## Seguridad y operación

- Cada token corresponde a una organización y tiene permisos de sólo lectura.
- La tabla guarda una referencia `env:NOMBRE_DEL_SECRETO`; el valor sólo vive en el entorno.
- La URL debe ser HTTP(S), sin usuario, query ni fragmento embebidos.
- Los paths de recursos deben ser relativos (`/api/...`); no pueden redirigir la llamada a
  otro host.
- Syna no registra tokens ni cabeceras de autorización en logs.
- Antes de devolver un resultado al modelo o guardarlo en la auditoría, Syna elimina campos
  de credenciales, tokens, secretos y datos de tarjetas. El CRM debe además omitirlos.
- Las futuras escrituras requieren endpoints idempotentes en el CRM y herramientas `WRITE`
  con aprobación explícita del usuario.
