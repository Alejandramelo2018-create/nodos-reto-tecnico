# Guía de la API — NODOS Reto Técnico

Referencia completa de endpoints para integrar el frontend (`http://localhost:5173`) con este backend (`http://localhost:8081`). Todo lo documentado aquí fue **verificado en vivo** contra el backend corriendo, no solo leído del código.

## Información general

- **Base URL**: `http://localhost:8081`
- **Content-Type** por defecto: `application/json` (se indica cuando un endpoint espera otra cosa)
- **Autenticación**: JWT en header `Authorization: Bearer <token>`. El token expira a las **24 horas**. El logout invalida el token en una lista negra **en memoria** del servidor (si el backend se reinicia, todos los tokens "deslogueados" vuelven a ser válidos hasta su expiración natural).
- **CORS**: habilitado para `http://localhost:5173` (métodos `GET, POST, PUT, DELETE, OPTIONS`, cualquier header, credenciales permitidas).
- Todas las rutas de recursos están ahora **en minúsculas**: `/nodos/contents`, `/nodos/expansionpacks`, `/nodos/users`, `/nodos/platform`, `/nodos/cart`, `/nodos/buys`, `/nodos/challenges`, `/nodos/subscriptionchallenges`. Las rutas viejas en mixed-case (`/nodos/Contents`, `/nodos/ExpansionPacks`, `/nodos/Users`) ya **no existen**. La feature `/nodos/subscriptions` (newsletter/beta testing) fue eliminada por completo.

### Formato de errores

| Código | Cuándo | Cuerpo |
|---|---|---|
| 400 | Falla de validación (`@Valid` en el body, ej. registro) | `{"campo": "mensaje", ...}` — un mapa por cada campo inválido |
| 401 | Sin token / token inválido / token invalidado por logout | `{"error": "Token invalidated"}` (para token invalidado) o body vacío según el caso |
| 401 | Login con credenciales incorrectas | `"Credenciales inválidas"` (string plano) |
| 401 | Acción bloqueada por una validación de negocio dentro de un controller (ej. no ser dueño de una compra, no ser ADMIN para finalizar un reto, o `currentPassword` incorrecta en `PUT /auth/me/password`) | el mensaje de la excepción, en texto plano (ver nota abajo) |
| 403 | Token válido pero rol insuficiente **según `SecurityConfig`** (ej. `POST /nodos/expansionpacks/create` sin rol ADMIN) | `{"timestamp":"...","status":403,"error":"Forbidden","path":"/..."}` (formato default de Spring) |
| 302 | Acceso **sin token** a un endpoint protegido | Redirect a una página de login HTML generada por Spring, **no JSON**. Ver nota abajo. |
| 500 | Cualquier excepción no controlada (`GlobalExceptionHandler`) | `"Internal error: <mensaje de la excepción>"` (string plano) |

> ⚠️ **Importante para el frontend**: si el usuario no está logueado y llama a un endpoint protegido, la respuesta es un **302 redirect** a una página HTML, no un 401 JSON limpio. Un `fetch` normal sigue el redirect automáticamente y termina con contenido HTML en vez de JSON — hay que manejar esto explícitamente (revisar `response.redirected` o el status antes de intentar `response.json()`), o el intento de parseo va a fallar de forma confusa. Esto pasa porque el login OAuth2 está activo sin un `AuthenticationEntryPoint` propio configurado para clientes JSON/SPA.
>
> ⚠️ Cualquier ruta que no exista (incluidas las viejas en mixed-case) devuelve **500** "Internal error: No static resource ..." en vez de un 404 limpio, porque `GlobalExceptionHandler` captura también la excepción interna de "recurso no encontrado" de Spring.
>
> ⚠️ **Hay dos mecanismos distintos de "acceso denegado" en la API, con status code distinto cada uno.** Si `SecurityConfig` bloquea la ruta/método por rol (ej. un usuario sin `ROLE_ADMIN` llamando `POST /nodos/expansionpacks/create`), la respuesta es **403** con el formato default de Spring. Pero si el bloqueo ocurre **dentro** de un controller (una validación de negocio, ej. "no sos dueño de esta compra" en `BuysController`, o "solo ADMIN puede finalizar un reto" en `SubscriptionChallengeController`), `GlobalExceptionHandler` tiene un handler específico para `AccessDeniedException` que responde **401** (no 403) con el mensaje en texto plano. No asumas que "sin permiso" siempre es 403 — depende de en qué capa se detectó.

---

## Auth (`/auth`) — público, excepto `GET /me`, `PUT /me`, `PUT /me/betatester` y `PUT /me/password`

| Método | Ruta | Body | Respuesta 200 |
|---|---|---|---|
| POST | `/auth/register` | `{"username","password","firstName","lastName","country","email","betaTester"}` (`betaTester` opcional, ver nota) | `{"token":"<jwt>"}` |
| POST | `/auth/login` | `{"username","password"}` | `{"token":"<jwt>"}` |
| POST | `/auth/register-admin` | igual que register | `"Admin user created"` o `"User promoted to admin"` (string plano) |
| POST | `/auth/logout` | — (header `Authorization`) | `"Logout exitoso"` |
| GET | `/auth/me` | — (requiere `Authorization: Bearer <token>`) | `200` + usuario autenticado (ver abajo) |
| PUT | `/auth/me` | `{"firstName","lastName","email","country"}` (requiere `Authorization: Bearer <token>`) | `200` + usuario autenticado actualizado (ver abajo) |
| PUT | `/auth/me/betatester` | `true`/`false` (boolean JSON plano, requiere `Authorization: Bearer <token>`) | `200` + usuario autenticado actualizado (ver abajo) |
| PUT | `/auth/me/password` | `{"currentPassword","newPassword"}` (`currentPassword` opcional, ver nota; requiere `Authorization: Bearer <token>`) | `200` + usuario autenticado actualizado (ver abajo) |
| GET | `/auth/oauth2/success` | — (requiere sesión OAuth2 activa) | `{"token","message","provider","email","name"}` |
| GET | `/oauth2/authorization/google` | — | 302 redirect a Google |
| GET | `/oauth2/authorization/meta` | — | 302 redirect a Facebook |

Tras un login OAuth2 exitoso, Spring redirige directo al **frontend** (no a `/auth/oauth2/success`), a la URL configurada en la variable de entorno `FRONTEND_URL` (`application.yml` → `frontend.url: ${FRONTEND_URL:http://localhost:5173/}`). Si el frontend corre en otro puerto/dominio, hay que actualizar `FRONTEND_URL` en `.env`.

### `GET /auth/me`

Uno de los cuatro endpoints de `/auth/**` que requieren estar autenticado (los otros son `PUT /auth/me`, `PUT /auth/me/betatester` y `PUT /auth/me/password`, ver más abajo) — pensado para que el frontend obtenga el `id` numérico del usuario logueado (necesario, por ejemplo, para armar `{"user":{"id":...}}` al llamar `POST /nodos/subscriptionchallenges/create`, ya que `/nodos/users/**` es ADMIN-only y no sirve para que un usuario normal se autoconsulte).

**Respuesta real** (`CurrentUserDTO`, sin `password` ni los campos internos de `UserDetails`):
```json
{
  "id": 3,
  "username": "meUser1",
  "firstName": "Me",
  "lastName": "User",
  "email": "meuser1@example.com",
  "country": "Colombia",
  "role": "ROLE_USER",
  "betaTester": false,
  "completedChallenges": 0,
  "hasPassword": true
}
```

`hasPassword` es nuevo: indica si el usuario tiene una contraseña propia guardada (`true` para cuentas registradas con `/auth/register`) o no (`false` para cuentas creadas solo por OAuth2/Google/Meta, donde `User.password` queda como `""`). Sirve para que el frontend sepa con certeza si debe mostrar "agregar contraseña" o "cambiar contraseña" en el perfil, sin depender de si la sesión actual entró por password o por OAuth2 (una misma cuenta puede haber usado ambos alguna vez).

> ⚠️ **Sin token o token malformado → 302** (no 401), exactamente igual que cualquier otro endpoint protegido de la app — ver la nota de "sin token en un endpoint protegido" al inicio de esta guía. La única forma de obtener un **401** JSON limpio en esta ruta es con un token que **sí es válido pero fue invalidado por logout** (`{"error": "Token invalidated"}`), igual que en el resto de la API. Verificado en vivo: token ausente → 302 a `/login`; token con formato inválido → 302 a `/login`; token deslogueado → 401. No se agregó un `AuthenticationEntryPoint` especial solo para esta ruta porque hubiera sido una inconsistencia respecto al resto de la API — si se necesita un verdadero 401 para "sin token" en toda la app, es un cambio más amplio a `SecurityConfig`, no algo específico de `/auth/me`. **Los otros tres endpoints de esta sección (`PUT /me`, `/me/betatester`, `/me/password`) comparten exactamente este mismo comportamiento** — no se repite la nota en cada uno.

### `PUT /auth/me`

Igual que `GET /auth/me`, identifica al usuario por el JWT (no recibe `id` por path). Actualiza **solo** `firstName`, `lastName`, `email` y `country` del usuario autenticado — `username` y `role` no se pueden cambiar por acá. Valida con las mismas reglas que `/auth/register` (`firstName`/`lastName` solo letras y espacios, `email` con formato válido, `country` 2-56 caracteres) → `400` con mapa de errores si falla.

**Body de ejemplo:**
```json
{
  "firstName": "Profile",
  "lastName": "Updated",
  "email": "newemail@example.com",
  "country": "Mexico"
}
```

**Respuesta real** (`CurrentUserDTO` actualizado, mismo shape que `GET /auth/me`).

> A diferencia de `PUT /nodos/users/{id}` (que solo actualiza `name`/`email` y descarta el resto silenciosamente), este endpoint sí aplica los 4 campos completos. Internamente también actualiza el campo interno `name` (`firstName + " " + lastName`) para que quede consistente con lo que ya se muestra en `Cart`/`Buy` — no es un campo separado que el frontend controle. No valida que el nuevo `email` sea único (a diferencia del registro) — no se pidió esa restricción.

### `PUT /auth/me/betatester`

Permite que **cualquier usuario autenticado** active o cancele su propia inscripción a beta testing, sin necesitar rol ADMIN — pensado para que más adelante el usuario pueda cancelarla por su cuenta. Body: booleano JSON plano (`true`/`false`), igual formato que ya usa `PUT /nodos/users/{id}/betatester`.

**Respuesta real** (`CurrentUserDTO`, mismo shape que `GET /auth/me`):
```json
{
  "id": 8,
  "username": "betauser2",
  "firstName": "NoBeta",
  "lastName": "User",
  "email": "betauser2@example.com",
  "country": "Colombia",
  "role": "ROLE_USER",
  "betaTester": true,
  "completedChallenges": 0,
  "hasPassword": true
}
```

> Este endpoint es distinto de `PUT /nodos/users/{id}/betatester` (que sigue existiendo, sin cambios, y sigue siendo **ADMIN-only** — pensado para que un admin fuerce el valor en cualquier usuario por su `id`). `PUT /auth/me/betatester` es el equivalente de autoservicio: solo puede cambiar el `betaTester` del usuario dueño del token, nunca el de otro. Verificado en vivo que ambos coexisten sin pisarse: un usuario normal recibe `403` al intentar `PUT /nodos/users/{id}/betatester` (regla de `SecurityConfig` por rol), pero `200` en `PUT /auth/me/betatester`.

### `PUT /auth/me/password`

Cambia la contraseña del usuario autenticado. Body: `{"currentPassword": "...", "newPassword": "..."}`.

- `newPassword` es **obligatoria** y sigue las mismas reglas que `/auth/register` (8-50 caracteres, mayúscula + minúscula + número + carácter especial) → `400` con mapa de errores si no cumple.
- `currentPassword` es **opcional según el estado de la cuenta**:
  - Si el usuario **ya tiene una contraseña** (`hasPassword: true` en `GET /auth/me`) — el caso normal de una cuenta registrada por `/auth/register` —, `currentPassword` es obligatoria y se valida contra el hash guardado (`PasswordEncoder.matches`). Si falta o no coincide, la respuesta es **401** `"La contraseña actual no coincide."` (mismo mecanismo de `AccessDeniedException` → 401 ya documentado al inicio de esta guía, no el 403 de `SecurityConfig`).
  - Si el usuario **no tiene contraseña** (`hasPassword: false` — cuenta creada solo por Google/Meta OAuth2, donde `User.password` queda `""`), `currentPassword` se puede omitir por completo y la nueva contraseña se guarda directamente, sin validar nada contra la anterior.

**Respuesta real** (`CurrentUserDTO` actualizado, mismo shape que `GET /auth/me` — incluye `hasPassword: true` una vez seteada la contraseña).

> Verificado en vivo (caso "usuario con contraseña"): `currentPassword` incorrecta o ausente → `401`; `currentPassword` correcta → `200` y la nueva contraseña ya sirve para `POST /auth/login`. El caso "usuario sin contraseña" (cuenta 100% OAuth2) se validó por lectura de código, no en vivo — requeriría un login real por Google/Meta para generar esa cuenta, que no es reproducible por curl en este entorno; la lógica es simétrica y usa el mismo booleano `hasPassword` ya verificado en `GET /auth/me`.

**Validaciones de `/auth/register`** (400 si fallan, un mensaje por campo):
- `username`: 3-30 caracteres, solo letras/números/`_`
- `password`: 8-50 caracteres, requiere mayúscula + minúscula + número + carácter especial
- `firstName`/`lastName`: **solo letras y espacios** (un número aquí, ej. `"Buyer3"`, dispara 400)
- `country`: 2-56 caracteres
- `email`: formato válido
- `betaTester`: **opcional**, sin validación — si no se envía (o se envía `null`), el usuario queda con `betaTester: false` (default de `User`). Se guarda directo, sin pasar por ningún endpoint aparte.

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

## Challenges (`/nodos/challenges`)

Público: `GET`. Requiere rol `ADMIN`: `POST`, `PUT`, `DELETE` (mismo patrón que Contents/Platform/ExpansionPacks).

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | `/nodos/challenges` | — | `200` + lista de retos |
| GET | `/nodos/challenges/{id}` | — | `200` + reto |
| POST | `/nodos/challenges/create` | ver campos abajo | `200` + `id` numérico |
| PUT | `/nodos/challenges/{id}` | ver campos abajo | `200` + reto actualizado (todos los campos se actualizan) |
| DELETE | `/nodos/challenges/{id}` | — | `200` + `"Challenge deleted successfully"` |

**Campos del body** (create/update):
```json
{
  "name": "Reto Jardin Zen",
  "start": "2026-08-01",
  "end": "2026-08-31",
  "description": "Construye el jardín más relajante",
  "imageURL": "http://example.com/reto1.png"
}
```

**Respuesta real**:
```json
{
  "id": 1,
  "name": "Reto Jardin Zen",
  "start": "2026-08-01",
  "end": "2026-08-31",
  "description": "Construye el jardín más relajante",
  "imageURL": "http://example.com/reto1.png",
  "deleted": false
}
```

`start`/`end` son fechas puras (`LocalDate`, formato `YYYY-MM-DD`, sin hora ni zona horaria) — se probó explícitamente en vivo con `java.util.Date` + `@Temporal(DATE)` primero y las fechas volvían corridas un día hacia atrás (por conversión de zona horaria al persistir); se cambió a `LocalDate` para evitarlo por completo. `imageURL` (con "URL" en mayúsculas al final) sí se serializa tal cual como `imageURL` — a diferencia de `URLImage` en ExpansionPacks, acá no hace falta ningún `@JsonProperty` especial porque el problema de Jackson solo aparece cuando el nombre **empieza** con dos o más mayúsculas seguidas.

---

## Subscription-Challenge (`/nodos/subscriptionchallenges`)

Relaciona un `User` con un `Challenge` (tabla `subscription_challenge`). Requiere estar **autenticado** (cualquier usuario, no solo ADMIN) para todo el CRUD — pero con una restricción adicional por el **valor de `status`** que se está seteando (ver abajo).

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | `/nodos/subscriptionchallenges` | — | `200` + lista completa |
| GET | `/nodos/subscriptionchallenges/{id}` | — | `200` + objeto |
| POST | `/nodos/subscriptionchallenges/create` | `{"user":{"id":<userId>},"challenge":{"id":<challengeId>}}` | `200` + `id` numérico. `status` queda en `INICIADO` por defecto, no se envía. |
| PUT | `/nodos/subscriptionchallenges/{id}` | `{"status":"<ESTADO>"}` (ver nota — `user`/`challenge` no se pueden reasignar por esta vía) | `200` + objeto actualizado |
| DELETE | `/nodos/subscriptionchallenges/{id}` | — | `200` + `"SubscriptionChallenge deleted successfully"` |
| GET | `/nodos/subscriptionchallenges/user/{userId}` | — | `200` + lista de inscripciones de ese usuario |

`status` es un enum: `INICIADO`, `EN_PROGRESO`, `FINALIZADO`, `FALLIDO`, `CANCELADO`.

> ⚠️ **Regla de autorización por transición de estado**: cualquier usuario autenticado puede crear la inscripción y moverla a `EN_PROGRESO` o `CANCELADO`. Para moverla a `FINALIZADO` o `FALLIDO`, el que llama al `PUT` tiene que tener `ROLE_ADMIN` — si no, la respuesta es **401** `"Solo un administrador puede marcar un reto como finalizado o fallido."` (ver la nota de "dos mecanismos de acceso denegado" al inicio de esta guía — este es el caso de `AccessDeniedException` lanzada dentro del controller, no el 403 de `SecurityConfig`). Verificado en vivo con un usuario no-ADMIN.
>
> Cuando el `status` pasa a `FINALIZADO` (y no lo estaba ya — un segundo `PUT` a `FINALIZADO` no vuelve a sumar), el backend incrementa automáticamente `completedChallenges` del `User` asociado. Confirmado en vivo: `GET /nodos/users/{id}/completedchallenges` sube en 1 exactamente una vez.
>
> `PUT` solo modifica `status` — el `user`/`challenge` originales de la inscripción no se pueden cambiar por este endpoint (si se envían, se ignoran).
>
> La respuesta incluye el `User` completo relacionado, pero **no incluye `password`** (ver sección Users más abajo — el campo se ocultó globalmente).

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
| GET | `/nodos/buys/{id}` | — | `200` + `BuyResponseDTO` (**401**, no 403, si la compra no es del usuario — ver nota de "dos mecanismos de acceso denegado" al inicio de la guía) |
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
| GET | `/nodos/users` | — | `200` + lista de usuarios (ya sin `password`, ver nota) |
| GET | `/nodos/users/{id}` | — | `200` + usuario |
| POST | `/nodos/users/create` | entidad `User` | `200` + `id` numérico |
| PUT | `/nodos/users/{id}` | `{"name","email"}` | `200` + usuario actualizado (ver nota) |
| DELETE | `/nodos/users/{id}` | — | `200` + `"User deleted successfully"` |
| PUT | `/nodos/users/{id}/role` | `"ROLE_ADMIN"` (string JSON plano) | `200` + usuario con rol actualizado |
| PUT | `/nodos/users/{id}/betatester` | `true`/`false` (boolean JSON plano) | `200` + usuario con `betaTester` actualizado (ADMIN-only — para que el propio usuario cambie el suyo, ver `PUT /auth/me/betatester` en la sección Auth) |
| GET | `/nodos/users/{id}/completedchallenges` | — | `200` + entero (cantidad de retos con `status: FINALIZADO`) |

**Campos nuevos en la entidad `User`**: `betaTester` (boolean, default `false`) y `completedChallenges` (entero, default `0`, se incrementa automáticamente desde `subscription_challenge` — ver esa sección).

> ✅ **Corregido en esta sesión**: `GET /nodos/users` (y `GET /nodos/users/{id}`) ya **no** devuelven el campo `"password"` — se agregó `@JsonIgnore` sobre el getter en `User.java`. Sigue devolviendo el resto de campos internos de `UserDetails` (`enabled`, `authorities`, `accountNonExpired`, etc.) sin filtrar, que no son sensibles pero son ruido para el frontend.
>
> ⚠️ **`PUT /nodos/users/{id}` solo actualiza `name` y `email`.** `firstName`, `lastName`, `country`, `username`, `role`, `betaTester`, `completedChallenges` se ignoran silenciosamente aunque los mandes en el body — usar los endpoints dedicados (`/role`, `/betatester`) para esos campos. `DELETE` funciona correctamente.

---

## Matriz de autorización (extraída de `SecurityConfig`)

| Recurso | Público | Requiere login | Requiere rol ADMIN |
|---|---|---|---|
| `/auth/**`, `/oauth2/**` | Todo excepto `GET /auth/me`, `PUT /auth/me`, `PUT /auth/me/betatester`, `PUT /auth/me/password` | `GET /auth/me`, `PUT /auth/me`, `PUT /auth/me/betatester`, `PUT /auth/me/password` | — |
| `/nodos/contents` | GET | — | POST/PUT/DELETE |
| `/nodos/platform` | GET | — | POST/PUT/DELETE |
| `/nodos/expansionpacks` | GET | — | POST/PUT/DELETE |
| `/nodos/challenges` | GET | — | POST/PUT/DELETE |
| `/nodos/users` | — | — | todo |
| `/nodos/cart` | — | todo | — |
| `/nodos/buys` | — | todo | — |
| `/nodos/subscriptionchallenges` | — | todo (con excepción, ver nota) | mover `status` a `FINALIZADO`/`FALLIDO` |

---

## Resumen de comportamientos conocidos (no corregidos en este cambio)

1. Sin token en un endpoint protegido → **302** a una página HTML, no 401 JSON.
2. Ruta inexistente → **500** "Internal error: No static resource ...", no 404.
3. `DELETE /nodos/contents/{id}` → siempre 500, lógica de existencia invertida.
4. `PUT /nodos/contents/{id}` y `PUT /nodos/users/{id}` → actualización parcial silenciosa (ignoran la mayoría de los campos del body).
5. ~~`GET /nodos/users` → expone `password` (hash)~~ — **corregido** (`@JsonIgnore` en `User.getPassword()`).
6. ~~`PUT /nodos/platform/{id}` → recursión infinita + fuga masiva de hash de contraseña~~ — **corregido** (se agregó `@JsonIgnore` en `Platform.cartDetails` y `Cart.details`).
7. `PUT /nodos/buys/{id}` → requiere `cart.id` explícito o falla con 500 (la recursión ya está corregida, este punto sigue pendiente).
8. Login (`/auth/login`) usa `username`, no `email` — ajustar el frontend en consecuencia.
9. `/nodos/cart/add` y `/nodos/cart/remove` reciben los IDs por **query string**, no por JSON body.
10. Logout invalida el token en memoria; se resetea si el backend reinicia.
11. `AccessDeniedException` lanzada dentro de un controller (dueño de compra, transición de estado de reto) responde **401**, no 403 — distinto del 403 que da `SecurityConfig` por rol insuficiente en la URL. Ver nota al inicio de la guía.
12. La feature `/nodos/subscriptions` (newsletter/beta testing) fue **eliminada por completo** de esta versión del backend.
13. `spring.jpa.hibernate.ddl-auto` ahora es `update` (no `create-drop`): los datos **persisten entre reinicios** del backend. El seeder de `ExpansionPack` sigue siendo seguro (solo siembra si la tabla está vacía).
