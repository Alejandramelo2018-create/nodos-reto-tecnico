# APIDog Test Report – NODOS API

Última ejecución: 2026-03-30 21:26 UTC

## Ambiente
- Java 17 / Spring Boot 3.3.0
- DB: PostgreSQL (`nodos_db`, esquema `reto`, `ddl-auto=create-drop`)
- Servidor en `http://localhost:8081`
- Usuarios creados durante la corrida:
  - Admin: `admin_nodos` / `AdminPass!23`
  - Usuario final: `usernodos` / `UserPass!23`

## Secuencia de pruebas (curl)
```
# 1) Admin helper y login
POST /auth/register-admin
POST /auth/login {admin}

# 2) Registro + login usuario
POST /auth/register
POST /auth/login {user}

# 3) Contenido (ADMIN)
POST   /nodos/Contents/create
GET    /nodos/Contents
GET    /nodos/Contents/{id}
PUT    /nodos/Contents/{id}
DELETE /nodos/Contents/{id}

# 4) Expansiones (ADMIN)
POST   /nodos/ExpansionPacks/create
GET    /nodos/ExpansionPacks
GET    /nodos/ExpansionPacks/{id}
PUT    /nodos/ExpansionPacks/{id}
DELETE /nodos/ExpansionPacks/{id}

# 5) Plataformas (ADMIN)
POST /nodos/platform/add
GET  /nodos/platform/{id}
GET  /nodos/platform

# 6) Carrito + compras (USER)
POST /nodos/cart/add
GET  /nodos/cart
POST /nodos/buys/purchase
GET  /nodos/cart (nuevo activo)
POST /nodos/buys/direct
GET  /nodos/buys
GET  /nodos/buys/{id}

# 7) Logout + validación
POST /auth/logout
GET  /nodos/cart  (debe fallar por token inválido)
```

El archivo `/tmp/curl_results.log` contiene la salida cruda de la última corrida.

## Resultados
| Paso | Endpoint | Estado | Resultado |
|------|----------|--------|-----------|
| 1 | `/auth/register-admin` | 200 | Admin creado/promovido. |
| 2 | `/auth/login` (admin) | 200 | JWT emitido. |
| 3 | `/auth/register` (user) | 200 | JWT emitido. |
| 4 | `/auth/login` (user) | 200 | JWT emitido. |
| 5 | CRUD plataforma | 200 | Alta/consulta/listado correctos. |
| 6 | POST `/nodos/Contents/create` | 200 | Contenido creado. |
| 7 | GET `/nodos/Contents` | 200 | Lista pública OK. |
| 8 | GET `/nodos/Contents/{id}` | 200 | Retorna body esperado. |
| 9 | **PUT `/nodos/Contents/{id}`** | **500** | `JSON parse error: Unexpected end-of-input in VALUE_STRING`. |
| 10 | **DELETE `/nodos/Contents/{id}`** | **500** | `Internal error: Content not found` pese a existir. |
| 11 | POST `/nodos/ExpansionPacks/create` | 200 | Expansión creada. |
| 12 | GET `/nodos/ExpansionPacks` | 200 | Lista pública OK. |
| 13 | GET `/nodos/ExpansionPacks/{id}` | 200 | Retorna body esperado. |
| 14 | **PUT `/nodos/ExpansionPacks/{id}`** | **500** | Mismo `JSON parse error`. |
| 15 | DELETE `/nodos/ExpansionPacks/{id}` | 200 | Eliminación correcta. |
| 16 | Cart add/get/purchase | 200 | Flujo completo (cierra carrito y crea uno nuevo). |
| 17 | Direct buy | 200 | Compra directa funciona. |
| 18 | GET `/nodos/buys` / `{id}` | 200 | Devuelve DTO sin datos sensibles. |
| 19 | POST `/auth/logout` | 200 | Token invalidado. |
| 20 | GET `/nodos/cart` (tras logout) | 401 | `{"error":"Token invalidated"}` esperado. |

## Pendientes de corrección
1. **PUT `/nodos/Contents/{id}`** rompe en deserialización. Revisar el body esperado (DTO vs entidad) y validar que Jackson reciba JSON válido.
2. **DELETE `/nodos/Contents/{id}`** siempre lanza “Content not found” por lógica invertida en `ContentsServiceImpl.deleteContent` (verifica `existsById` al revés).
3. **PUT `/nodos/ExpansionPacks/{id}`** falla con el mismo error de parseo; revisar controlador/servicio y el payload esperado.
4. Considerar ocultar `password` y autoridades en `GET /nodos/Users` si se requiere sanitizar la respuesta admin.

Una vez corregidos los puntos anteriores repetir la suite curl y actualizar este reporte.
