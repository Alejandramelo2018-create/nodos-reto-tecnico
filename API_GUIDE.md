# Guía de la API — NODOS Reto Técnico

Referencia completa de endpoints para integrar el frontend (`http://localhost:5173`) con este backend (`http://localhost:8081`). Todo lo documentado aquí fue **verificado en vivo** contra el backend corriendo, no solo leído del código.

## Información general

- **Base URL**: `http://localhost:8081`
- **Content-Type** por defecto: `application/json` (se indica cuando un endpoint espera otra cosa)
- **Autenticación**: JWT en header `Authorization: Bearer <token>`. El token expira a las **24 horas**. El logout invalida el token en una lista negra **en memoria** del servidor (si el backend se reinicia, todos los tokens "deslogueados" vuelven a ser válidos hasta su expiración natural).
- **CORS**: habilitado para `http://localhost:5173` (métodos `GET, POST, PUT, DELETE, OPTIONS`, cualquier header, credenciales permitidas).
- Todas las rutas de recursos están ahora **en minúsculas**: `/nodos/contents`, `/nodos/expansionpacks`, `/nodos/users`, `/nodos/platform`, `/nodos/cart`, `/nodos/buys`, `/nodos/subscriptions`. Las rutas viejas en mixed-case (`/nodos/Contents`, `/nodos/ExpansionPacks`, `/nodos/Users`) ya **no existen**.

### Formato de errores

| Código | Cuándo | Cuerpo |
|---|---|---|
| 400 | Falla de validación (`@Valid` en el body, ej. registro) | `{"campo": "mensaje", ...}` — un mapa por cada campo inválido |
| 401 | Sin token / token inválido / token invalidado por logout | `{"error": "Token invalidated"}` (para token invalidado) o body vacío según el caso |
| 401 | Login con credenciales incorrectas | `"Credenciales inválidas"` (string plano) |
| 403 | Token válido pero rol insuficiente | `{"timestamp":"...","status":403,"error":"Forbidden","path":"/..."}` (formato default de Spring) |
| 302 | Acceso **sin token** a un endpoint protegido | Redirect a una página de login HTML generada por Spring, **no JSON**. Ver nota abajo. |
| 500 | Cualquier excepción no controlada (`GlobalExceptionHandler`) | `"Internal error: <mensaje de la excepción>"` (string plano) |

> ⚠️ **Importante para el frontend**: si el usuario no está logueado y llama a un endpoint protegido, la respuesta es un **302 redirect** a una página HTML, no un 401 JSON limpio. Un `fetch` normal sigue el redirect automáticamente y termina con contenido HTML en vez de JSON — hay que manejar esto explícitamente (revisar `response.redirected` o el status antes de intentar `response.json()`), o el intento de parseo va a fallar de forma confusa. Esto pasa porque el login OAuth2 está activo sin un `AuthenticationEntryPoint` propio configurado para clientes JSON/SPA.
>
> ⚠️ Cualquier ruta que no exista (incluidas las viejas en mixed-case) devuelve **500** "Internal error: No static resource ..." en vez de un 404 limpio, porque `GlobalExceptionHandler` captura también la excepción interna de "recurso no encontrado" de Spring.

---

## Auth (`/auth`) — público

| Método | Ruta | Body | Respuesta 200 |
|---|---|---|---|
| POST | `/auth/register` | `{"username","password","firstName","lastName","country","email"}` | `{"token":"<jwt>"}` |
| POST | `/auth/login` | `{"username","password"}` | `{"token":"<jwt>"}` |
| POST | `/auth/register-admin` | igual que register | `"Admin user created"` o `"User promoted to admin"` (string plano) |
| POST | `/auth/logout` | — (header `Authorization`) | `"Logout exitoso"` |
| GET | `/auth/oauth2/success` | — (requiere sesión OAuth2 activa) | `{"token","message","provider","email","name"}` |
| GET | `/oauth2/authorization/google` | — | 302 redirect a Google |
| GET | `/oauth2/authorization/meta` | — | 302 redirect a Facebook |

**Validaciones de `/auth/register`** (400 si fallan, un mensaje por campo):
- `username`: 3-30 caracteres, solo letras/números/`_`
- `password`: 8-50 caracteres, requiere mayúscula + minúscula + número + carácter especial
- `firstName`/`lastName`: **solo letras y espacios** (un número aquí, ej. `"Buyer3"`, dispara 400)
- `country`: 2-56 caracteres
- `email`: formato válido

> ⚠️ **Login requiere `username`, no `email`.** El body de `/auth/login` es `{"username","password"}` — no existe login por email en el backend (`CustomUserDetailsService` busca únicamente por username). Si el formulario de frontend pide "correo", hay que mapearlo al campo `username` al enviarlo, o el login siempre devuelve 401.

---

## Contents (`/nodos/contents`)

Público: `GET`. Requiere rol `ADMIN`: `POST`, `PUT`, `DELETE`.

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | `/nodos/contents` | — | `200` + `[{"id","section","title","description","image","deleted"}]` |
| GET | `/nodos/contents/{id}` | — | `200` + objeto `Content` |
| POST | `/nodos/contents/create` | `{"section","title","description","image"}` | `200` + `id` numérico |
| PUT | `/nodos/contents/{id}` | `{"section","title","description","image"}` | `200` + objeto actualizado (ver nota) |
| DELETE | `/nodos/contents/{id}` | — (header `Authorization`) | ver nota — **actualmente siempre falla** |

> ⚠️ **`PUT` solo actualiza `title` y `description`.** Aunque envíes `section`/`image` en el body, esos dos campos se ignoran silenciosamente y quedan con su valor anterior (bug confirmado en vivo).
>
> ⚠️ **`DELETE` está roto: siempre devuelve 500 `"Internal error: Content not found"` incluso cuando el contenido existe** (lógica invertida en `ContentsServiceImpl.deleteContent` — confirmado en vivo, no depende de la ruta). No hay forma de borrar un content por esta vía hasta que se corrija esa condición.

---

## Platform (`/nodos/platform`)

Público: `GET`. Requiere rol `ADMIN`: `POST`, `PUT`, `DELETE`.

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | `/nodos/platform` | — | `200` + `[{"id","name"}]` (DTO, sin `url`) |
| GET | `/nodos/platform/{id}` | — | `200` + `{"id","name"}` (DTO) |
| POST | `/nodos/platform/add` | `{"name","url"}` | `200` + `id` numérico |
| PUT | `/nodos/platform/{id}` | `{"name","url"}` | `200` + entidad completa (`id`,`name`,`url`,`deleted`) |
| DELETE | `/nodos/platform/{id}` | — | `200` + `"Platform deleted successfully"` |

> Corregido: `PUT /nodos/platform/{id}` devuelve la entidad `Platform`, pero ya **no** incluye `cartDetails` (se agregó `@JsonIgnore`, igual que ya tenía `ExpansionPack.cartDetails`). Antes, si la plataforma tenía algún item de carrito asociado, la respuesta entraba en un ciclo infinito de serialización que repetía el hash bcrypt de la contraseña del usuario dueño del carrito cientos de veces — verificado y corregido en esta misma sesión (también se aplicó el mismo fix a `Cart.details`, usado por `PUT /nodos/buys/{id}`).

---

## ExpansionPacks (`/nodos/expansionpacks`)

Público: `GET`. Requiere rol `ADMIN`: `POST`, `PUT`, `DELETE`.

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | `/nodos/expansionpacks` | — | `200` + lista de packs |
| GET | `/nodos/expansionpacks/{id}` | — | `200` + pack |
| POST | `/nodos/expansionpacks/create` | ver campos abajo | `200` + `id` numérico |
| PUT | `/nodos/expansionpacks/{id}` | ver campos abajo | `200` + pack actualizado |
| DELETE | `/nodos/expansionpacks/{id}` | — | `200` + `"Expansion Pack deleted successfully"` |

**Campos del body** (create/update, todos actualizables en `PUT`):
```json
{
  "name": "Pack Uno",
  "description": "desc",
  "platforms": "PC / Mac / Consolas",
  "price": 25.5,
  "category": "RPG",
  "publicationDate": "2026-01-01",
  "language": "es",
  "URLImage": "http://example.com/img.png",
  "characteristics": ["Multijugador", "4K"]
}
```

**Respuesta real** (verificada, ya sin `cartDetails` — se ocultó a propósito para evitar el bug de recursión, mismo fix ya aplicado también en Platform/Cart):
```json
{
  "id": 1,
  "name": "Pack Uno",
  "description": "desc",
  "platforms": "PC / Mac / Consolas",
  "price": 25.5,
  "category": "RPG",
  "publicationDate": "2026-01-01",
  "language": "es",
  "deleted": false,
  "characteristics": ["Multijugador", "4K"],
  "URLImage": "http://example.com/img.png"
}
```

> ⚠️ El campo se llama exactamente `URLImage` (mayúsculas tal cual) tanto para enviar como para leer — no `urlImage` ni `urlimage`. Mandarlo con otra capitalización hace que el backend lo reciba como `null` sin ningún error.

---

## Subscriptions (`/nodos/subscriptions`)

Público: solo `POST /create`. Todo lo demás requiere rol `ADMIN`.

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| POST | `/nodos/subscriptions/create` | `{"email","name","subscriptionType","country","consentMarketing"}` | `200` + `id` numérico |
| GET | `/nodos/subscriptions` | — (ADMIN) | `200` + lista |
| GET | `/nodos/subscriptions/{id}` | — (ADMIN) | `200` + objeto |
| PUT | `/nodos/subscriptions/{id}` | mismos campos | `200` + objeto actualizado |
| DELETE | `/nodos/subscriptions/{id}` | — (ADMIN) | `200` + `"Subscription deleted successfully"` |

`subscriptionType` es un enum: `BETA_TESTING`, `FOCUS_GROUP`, `SIMMER_CHALLENGE`. El campo `status` lo asigna el servidor (`PENDING` por defecto, no se envía en el request).

---

## Cart (`/nodos/cart`) — requiere estar autenticado (cualquier usuario)

| Método | Ruta | Params/Body | Respuesta |
|---|---|---|---|
| GET | `/nodos/cart` | — | `200` + `CartResponseDTO` (ver abajo) |
| POST | `/nodos/cart/add` | **query params** `expansionId`, `platformId` (no JSON body) | `200` + carrito actualizado |
| POST | `/nodos/cart/remove` | **query param** `expansionId` | `200` + carrito actualizado |
| POST | `/nodos/cart/clear` | — | `200`, sin contenido |

**Respuesta de carrito** (segura, siempre vía DTO, no expone entidades crudas):
```json
{
  "id": 1,
  "status": "activo",
  "user": {"id": 2, "name": "Buyer User", "email": "...", "username": "...", "country": "..."},
  "items": [
    {"id": 1, "expansionPack": {"id":1,"name":"...","description":"...","price":25.5}, "platform": {"id":1,"name":"Steam"}}
  ],
  "total": 25.5
}
```

---

## Buys (`/nodos/buys`) — requiere estar autenticado

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | `/nodos/buys` | — | `200` + lista de `BuyResponseDTO` del usuario logueado |
| GET | `/nodos/buys/{id}` | — | `200` + `BuyResponseDTO` (403 si la compra no es del usuario) |
| POST | `/nodos/buys/purchase` | body **texto plano** (ej. `CARD`), header `Content-Type: text/plain` | `200` + `BuyResponseDTO`, vacía el carrito activo y crea uno nuevo |
| POST | `/nodos/buys/direct` | `{"expansionId","platformId","paymentMethod"}` (JSON) | `200` + `BuyResponseDTO` |
| PUT | `/nodos/buys/{id}` | entidad `Buy` completa, **incluyendo `"cart":{"id": <id_real_del_cart>}`** | `200` + entidad `Buy` (ver advertencia) |
| DELETE | `/nodos/buys/{id}` | — | `200` + `"Buy deleted successfully"` |

**Respuesta de compra** (`BuyResponseDTO`, segura):
```json
{
  "id": 1,
  "purchaseDate": "2026-07-23T19:38:26.563+00:00",
  "totalPrice": 25.5,
  "paymentMethod": "CARD",
  "status": "completado",
  "items": [
    {"id":2,"quantity":1,"expansionPack":{"id":1,"name":"...","description":"...","price":25.5},"platform":{"id":1,"name":"Steam"}}
  ]
}
```

> ⚠️ **`PUT /nodos/buys/{id}` exige mandar `cart.id` explícitamente en el body**, o el update falla con 500 por violar la restricción `NOT NULL` de `cart_id` (`BuysServiceImpl.updateBuy` sobreescribe el `cart` existente con lo que venga en el request, incluso si viene vacío). Esta ruta devuelve la entidad `Buy` cruda (no un DTO); el riesgo de recursión infinita que tenía esta ruta a través de `Cart.details` ya se corrigió en esta sesión (ver nota de Platform).
>
> Confirmado en vivo: `getAuthenticatedUsername()` en `BuysController` sí soporta tokens JWT normales (no solo sesiones OAuth2) — busca al usuario por email y si no lo encuentra, por username. Ya no aplica una limitación vieja que aparecía en documentación anterior del proyecto.

---

## Users (`/nodos/users`) — todo requiere rol `ADMIN`

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | `/nodos/users` | — | `200` + lista de usuarios **(ver advertencia)** |
| GET | `/nodos/users/{id}` | — | `200` + usuario |
| POST | `/nodos/users/create` | entidad `User` | `200` + `id` numérico |
| PUT | `/nodos/users/{id}` | `{"name","email"}` | `200` + usuario actualizado (ver nota) |
| DELETE | `/nodos/users/{id}` | — | `200` + `"User deleted successfully"` |
| PUT | `/nodos/users/{id}/role` | `"ROLE_ADMIN"` (string JSON plano) | `200` + usuario con rol actualizado |

> 🔴 **`GET /nodos/users` (y `GET /nodos/users/{id}`) devuelven el hash bcrypt de la contraseña de cada usuario** en el campo `"password"`, además de los campos internos de `UserDetails` (`enabled`, `authorities`, `accountNonExpired`, etc.) sin ningún filtro. El frontend nunca debería mostrar ni loguear ese campo. Confirmado en vivo.
>
> ⚠️ **`PUT /nodos/users/{id}` solo actualiza `name` y `email`.** `firstName`, `lastName`, `country`, `username`, `role` se ignoran silenciosamente aunque los mandes en el body (bug confirmado en vivo — a diferencia de lo que decía documentación previa del proyecto, `DELETE` sí funciona correctamente en la versión actual).

---

## Matriz de autorización (extraída de `SecurityConfig`)

| Recurso | Público | Requiere login | Requiere rol ADMIN |
|---|---|---|---|
| `/auth/**`, `/oauth2/**` | Todo | — | — |
| `/nodos/contents` | GET | — | POST/PUT/DELETE |
| `/nodos/platform` | GET | — | POST/PUT/DELETE |
| `/nodos/expansionpacks` | GET | — | POST/PUT/DELETE |
| `/nodos/subscriptions` | solo POST `/create` | — | resto (GET/PUT/DELETE) |
| `/nodos/users` | — | — | todo |
| `/nodos/cart` | — | todo | — |
| `/nodos/buys` | — | todo | — |

---

## Resumen de comportamientos conocidos (no corregidos en este cambio)

1. Sin token en un endpoint protegido → **302** a una página HTML, no 401 JSON.
2. Ruta inexistente → **500** "Internal error: No static resource ...", no 404.
3. `DELETE /nodos/contents/{id}` → siempre 500, lógica de existencia invertida.
4. `PUT /nodos/contents/{id}` y `PUT /nodos/users/{id}` → actualización parcial silenciosa (ignoran la mayoría de los campos del body).
5. `GET /nodos/users` → expone `password` (hash) y campos internos de `UserDetails`.
6. ~~`PUT /nodos/platform/{id}` → recursión infinita + fuga masiva de hash de contraseña~~ — **corregido** (se agregó `@JsonIgnore` en `Platform.cartDetails` y `Cart.details`).
7. `PUT /nodos/buys/{id}` → requiere `cart.id` explícito o falla con 500 (la recursión ya está corregida, este punto sigue pendiente).
8. Login (`/auth/login`) usa `username`, no `email` — ajustar el frontend en consecuencia.
9. `/nodos/cart/add` y `/nodos/cart/remove` reciben los IDs por **query string**, no por JSON body.
10. Logout invalida el token en memoria; se resetea si el backend reinicia.
