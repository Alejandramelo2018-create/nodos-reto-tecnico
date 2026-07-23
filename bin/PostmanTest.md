# Matriz de pruebas backend - NODOS

Base URL: `http://localhost:8081`  
Header JSON estándar: `Content-Type: application/json`

## 1) Autenticación (todas las maneras)

| Caso | Método | URL | Headers | Body | Respuesta esperada |
|---|---|---|---|---|---|
| Registro válido | POST | `/auth/register` | `Content-Type: application/json` | `{"username":"user_demo","password":"StrongPass1!","firstName":"Ana","lastName":"Perez","country":"Argentina","email":"user_demo@example.com"}` | `200` + `{"token":"<jwt>"}` |
| Registro inválido (validaciones) | POST | `/auth/register` | `Content-Type: application/json` | `{"username":"a","password":"123","firstName":"","lastName":"","country":"","email":"bad"}` | `400` + mapa de errores por campo (`username`,`password`,`firstName`,`lastName`,`country`,`email`) |
| Login estándar | POST | `/auth/login` | `Content-Type: application/json` | `{"username":"user_demo","password":"cualquier_valor"}` | `200` + `{"token":"<jwt>"}` |
| OAuth2 Google inicio | GET | `/oauth2/authorization/google` | - | - | `302` redirect a Google (`accounts.google.com`) |
| OAuth2 Meta inicio | GET | `/oauth2/authorization/meta` | - | - | `302` redirect a Facebook (`facebook.com`) |
| OAuth2 success (manual, luego de login social en navegador) | GET | `/auth/oauth2/success` | Cookie/sesión OAuth2 del navegador | - | `200` + `{"token":"<jwt>","message":"OAuth2 login successful","provider":"google|meta","email":"...","name":"..."}` |

---

## 2) Seguridad (acceso sin token)

| Caso | Método | URL | Headers | Respuesta esperada |
|---|---|---|---|---|
| Cart sin token | GET | `/nodos/cart` | - | `302` (redirección a login) |
| Users sin token | GET | `/nodos/Users` | - | `302` (redirección a login) |
| Contents sin token | GET | `/nodos/Contents` | - | `200` (comportamiento actual) |

---

## 3) Plataforma (con JWT)

Usar: `Authorization: Bearer <TOKEN_STD>`

| Caso | Método | URL | Headers | Body | Respuesta esperada |
|---|---|---|---|---|---|
| Crear plataforma | POST | `/nodos/platform/add` | `Authorization`, `Content-Type` | `{"name":"Steam","url":"https://store.steampowered.com"}` | `200` + id numérico |
| Obtener por id | GET | `/nodos/platform/{id}` | `Authorization` | - | `200` + objeto plataforma |
| Actualizar | PUT | `/nodos/platform/{id}` | `Authorization`, `Content-Type` | `{"name":"Steam Updated","url":"https://example.com"}` | `200` + objeto actualizado |
| Eliminar | DELETE | `/nodos/platform/{id}` | `Authorization` | - | `200` + `"Platform deleted successfully"` |

---

## 4) Expansion Packs (con JWT)

| Caso | Método | URL | Headers | Body | Respuesta esperada |
|---|---|---|---|---|---|
| Crear pack | POST | `/nodos/ExpansionPacks/create` | `Authorization`, `Content-Type` | `{"name":"Pack One","description":"desc","distributor":"EA","price":20.5,"category":"RPG","publicationDate":"2026-01-01","language":"es"}` | `200` + id numérico |
| Obtener por id | GET | `/nodos/ExpansionPacks/{id}` | `Authorization` | - | `200` + objeto pack |
| Actualizar | PUT | `/nodos/ExpansionPacks/{id}` | `Authorization`, `Content-Type` | `{"name":"Pack One Updated","description":"desc updated"}` | `200` + objeto actualizado |
| Eliminar | DELETE | `/nodos/ExpansionPacks/{id}` | `Authorization` | - | `200` + `"Expansion Pack deleted successfully"` |

---

## 5) Contents (comportamiento actual)

| Caso | Método | URL | Headers | Body | Respuesta esperada |
|---|---|---|---|---|---|
| Listar contents | GET | `/nodos/Contents` | - | - | `200` + lista |
| Eliminar content existente (con JWT) | DELETE | `/nodos/Contents/{id}` | `Authorization` | - | `500` + `"Internal error: Content not found"` (comportamiento actual detectado) |

---

## 6) Users (comportamiento actual)

| Caso | Método | URL | Headers | Body | Respuesta esperada |
|---|---|---|---|---|---|
| Eliminar user existente (con JWT) | DELETE | `/nodos/Users/{id}` | `Authorization` | - | `500` + `"Internal error: User no found"` (comportamiento actual detectado) |

---

## 7) Carrito (con JWT)

| Caso | Método | URL | Headers | Params/Body | Respuesta esperada |
|---|---|---|---|---|---|
| Ver carrito inicial | GET | `/nodos/cart` | `Authorization` | - | `200` + `total: 0.0` |
| Agregar al carrito | POST | `/nodos/cart/add` | `Authorization` | Query: `expansionId`, `platformId` | `200` + `items` incrementa + total recalculado |
| Quitar del carrito | POST | `/nodos/cart/remove` | `Authorization` | Query: `expansionId` | `200` + `items` decrementa + total recalculado |
| Vaciar carrito | POST | `/nodos/cart/clear` | `Authorization` | - | `200` |

---

## 8) Buys (JWT vs OAuth2)

| Caso | Método | URL | Headers | Respuesta esperada |
|---|---|---|---|---|
| Listar buys con JWT estándar | GET | `/nodos/buys` | `Authorization: Bearer <TOKEN_STD>` | `500` + `"Usuario no autenticado"` (comportamiento actual) |
| Obtener buy por id con JWT estándar | GET | `/nodos/buys/{id}` | `Authorization: Bearer <TOKEN_STD>` | `500` + `"Usuario no autenticado"` |
| Listar buys con sesión OAuth2 válida | GET | `/nodos/buys` | cookie/sesión OAuth2 | `200` + lista de compras del usuario |
| Obtener buy por id con sesión OAuth2 válida | GET | `/nodos/buys/{id}` | cookie/sesión OAuth2 | `200` + compra |

---

## Variables sugeridas en Postman

- `BASE_URL = http://localhost:8081`
- `TOKEN_STD = <token de /auth/login o /auth/register>`
- `PLATFORM_ID = <id creado>`
- `PACK_ID = <id creado>`
- `CONTENT_ID = <id creado>`
- `USER_ID = <id creado>`
- `BUY_ID = <id creado>`
