# Syna embebido en el CRM

## Arquitectura obligatoria

El navegador no llama a Syna directamente. El componente de chat del CRM llama a su propio
backend (BFF); el BFF valida la sesión del usuario, crea un JWT breve y llama a Syna.

```text
CRM UI -> CRM BFF -> POST Syna /v1/chat
                         |
                         +-> Syna memory + conversations
                         +-> CRM capability API (lecturas en vivo)
```

No envíes cookies de sesión, tokens de usuario del CRM ni registros completos por el browser.
Syna consulta datos vivos mediante el conector CRM cuando los necesita.

## JWT que debe emitir el CRM

El CRM usa su proveedor OIDC existente o publica un JWKS HTTPS. El JWT debe estar firmado
con una clave asimétrica, tener duración máxima de cinco minutos y contener:

```json
{
  "iss": "https://crm.example.com",
  "aud": ["syna-embedded"],
  "sub": "crm-user-stable-id",
  "organization_id": "UUID_DE_LA_ORGANIZACION_EN_SYNA",
  "application_id": "UUID_OPCIONAL",
  "project_id": "UUID_OPCIONAL",
  "session_id": "UUID_OPCIONAL",
  "name": "Nombre visible",
  "roles": ["SYNA_SALES"],
  "exp": 0,
  "iat": 0,
  "jti": "id-unico"
}
```

`organization_id` es obligatorio y debe existir previamente en Syna. Syna crea o actualiza
el usuario local usando `(organization_id, sub)`; nunca acepta un `userId` enviado por el
browser. Los roles permitidos son `SYNA_SALES`, `SYNA_MARKETING`, `SYNA_OPERATIONS`,
`SYNA_ANALYTICS`, `SYNA_FINANCE`, `SYNA_ACCOUNTING`, `SYNA_CREATIVE`, `SYNA_DEVELOPER`
o `SYNA_ADMIN`. Sólo `SYNA_ADMIN` puede usar el modo General.

## Configurar Syna

En el entorno de Syna:

```dotenv
SYNA_EMBEDDED_ENABLED=true
SYNA_EMBEDDED_JWT_ISSUER=https://crm.example.com
SYNA_EMBEDDED_JWK_SET_URI=https://crm.example.com/.well-known/jwks.json
SYNA_EMBEDDED_JWT_AUDIENCE=syna-embedded
CRM_ENABLED=true
```

Cuando `SYNA_EMBEDDED_ENABLED=true`, Syna bloquea las rutas heredadas `/api/**`; sólo deja
`/v1/**` con Bearer JWT y `/actuator/health`/`info` sin autenticación. Esto evita que el CRM
embebido pueda falsear usuario u organización en un request body.

Además, provisioná la organización y su agente por defecto en Syna y configurá la conexión
Syna -> CRM como explica [SYNA_CRM_INTEGRATION.md](SYNA_CRM_INTEGRATION.md).

## Endpoints para el BFF del CRM

```http
POST /v1/chat
Authorization: Bearer <jwt-corto>
Content-Type: application/json
```

```json
{
  "conversationId": null,
  "message": "¿Qué debería hacer con este lead?",
  "mode": "SALES",
  "reasoningProfile": "NORMAL",
  "context": {
    "entityType": "lead",
    "entityId": "lead_123",
    "screen": "crm/leads/detail"
  }
}
```

El contexto acepta sólo `entityType`, `entityId` y `screen`; no incluyas datos del lead.
Syna recibe el ID y llama a `get_crm_lead` si necesita datos actuales. La respuesta devuelve
el `conversationId`; el CRM debe conservarlo para el próximo turno.

```http
GET /v1/conversations?limit=40
GET /v1/conversations/{conversationId}
Authorization: Bearer <jwt-corto>
```

Los dos endpoints derivan la identidad del JWT y sólo devuelven conversaciones del usuario
actual dentro de su organización.

## Implementación del CRM

1. Creá un componente de chat nativo en el CRM; no uses el token de Syna en el navegador.
2. Creá un endpoint BFF, por ejemplo `POST /api/syna/chat`, que valide la sesión CRM.
3. El BFF emite el JWT anterior y llama a `POST /v1/chat` con el token. No permita que el
   frontend seleccione `organization_id`, `sub` ni roles.
4. Aplicá en el CRM los permisos del usuario al responder las llamadas de datos en vivo de
   Syna. El token de conexión de Syna es sólo lectura y por organización; si necesitás
   permisos por usuario, implementá token exchange o una capability API que valide el actor.
5. Configurá rate limits, auditoría por `jti` y revocación/rotación de claves en el CRM.

La primera versión de `/v1/chat` responde al terminar la generación. Streaming SSE es una
siguiente mejora de UX, no un requisito de seguridad para integrar el widget.
