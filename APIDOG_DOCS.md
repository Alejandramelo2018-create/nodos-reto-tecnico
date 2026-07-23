# NODOS API - Documentación para APIDog

## Información General

- **Base URL**: `http://localhost:8081`
- **Autenticación**: JWT Bearer Token
- **Content-Type**: `application/json`

---

## Autenticación

### 1. Registro de Usuario

**POST** `/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "username": "usernodos",
  "password": "UserPass!23",
  "firstName": "User",
  "lastName": "Tester",
  "country": "Colombia",
  "email": "usernodos@example.com"
}
```

**Respuesta (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Validaciones:**
- username: 3-30 caracteres, solo letras, números y guiones bajos
- password: 8-50 caracteres, debe contener mayúscula, minúscula, número y carácter especial
- firstName/lastName: solo letras y espacios
- country: 2-56 caracteres
- email: formato válido de email

---

### 2. Login de Usuario

**POST** `/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "username": "usernodos",
  "password": "UserPass!23"
}
```

**Respuesta (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Respuesta Error (401):**
```json
"Credenciales inválidas"
```

---

### 3. Registro/Promoción de Admin

**POST** `/auth/register-admin`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

**Body:**
```json
{
  "username": "admin_nodos",
  "password": "AdminPass!23",
  "firstName": "Admin",
  "lastName": "Tester",
  "country": "Colombia",
  "email": "admin_nodos@example.com"
}
```

**Respuesta (200):**
```json
"Admin user created"
```

ó

```json
"User promoted to admin"
```

---

### 4. Logout

**POST** `/auth/logout`

**Headers:**
```
Authorization: Bearer <TOKEN>
```

**Respuesta (200):**
```json
"Logout exitoso"
```

---

## Contenidos (Contents)

### 5. Crear Contenido (Admin)

**POST** `/nodos/Contents/create`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

**Body:**
```json
{
  "section": "Noticias",
  "title": "Lanzamiento",
  "description": "Detalle del contenido",
  "image": "imagen.jpg"
}
```

**Respuesta (200):**
```json
1
```

---

### 6. Listar Contenidos (Público)

**GET** `/nodos/Contents`

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "section": "Noticias",
    "title": "Lanzamiento",
    "description": "Detalle del contenido",
    "image": "imagen.jpg",
    "deleted": false
  }
]
```

---

### 7. Obtener Contenido por ID

**GET** `/nodos/Contents/{id}`

**Parámetros:**
- `id` (path): ID del contenido

**Respuesta (200):**
```json
{
  "id": 1,
  "section": "Noticias",
  "title": "Lanzamiento",
  "description": "Detalle del contenido",
  "image": "imagen.jpg",
  "deleted": false
}
```

---

### 8. Actualizar Contenido (Admin)

**PUT** `/nodos/Contents/{id}`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

**Body:**
```json
{
  "title": "Título actualizado",
  "description": "Descripción actualizada"
}
```

**Respuesta (200):**
```json
{
  "id": 1,
  "section": "Noticias",
  "title": "Título actualizado",
  "description": "Descripción actualizada",
  "image": "imagen.jpg",
  "deleted": false
}
```

**Error (500):**
```json
"Internal error: JSON parse error: Unexpected end-of-input in VALUE_STRING"
```

---

### 9. Eliminar Contenido (Admin)

**DELETE** `/nodos/Contents/{id}`

**Headers:**
```
Authorization: Bearer <ADMIN_TOKEN>
```

**Respuesta (200):**
```json
"Content deleted successfully"
```

**Error (500):**
```json
"Internal error: Content not found"
```

---

## Plataformas

### 10. Crear Plataforma (Admin)

**POST** `/nodos/platform/add`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

**Body:**
```json
{
  "name": "Steam",
  "url": "https://store.steampowered.com"
}
```

**Respuesta (200):**
```json
1
```

---

### 11. Listar Plataformas

**GET** `/nodos/platform`

**Headers:**
```
Authorization: Bearer <USER_TOKEN>
```

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "name": "Steam"
  }
]
```

---

### 12. Obtener Plataforma por ID

**GET** `/nodos/platform/{id}`

**Headers:**
```
Authorization: Bearer <USER_TOKEN>
```

**Respuesta (200):**
```json
{
  "id": 1,
  "name": "Steam"
}
```

---

### 13. Actualizar Plataforma (Admin)

**PUT** `/nodos/platform/{id}`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

**Body:**
```json
{
  "name": "Steam Actualizado",
  "url": "https://ejemplo.com"
}
```

**Respuesta (200):**
```json
{
  "id": 1,
  "name": "Steam Actualizado",
  "url": "https://ejemplo.com"
}
```

---

### 14. Eliminar Plataforma (Admin)

**DELETE** `/nodos/platform/{id}`

**Headers:**
```
Authorization: Bearer <ADMIN_TOKEN>
```

**Respuesta (200):**
```json
"Platform deleted successfully"
```

---

## Expansiones (Expansion Packs)

### 15. Crear Expansión (Admin)

**POST** `/nodos/ExpansionPacks/create`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

**Body:**
```json
{
  "name": "PackUno",
  "description": "Descripción del pack",
  "distributor": "EA",
  "price": 25.5,
  "category": "RPG",
  "publicationDate": "2026-01-01",
  "language": "ES"
}
```

**Respuesta (200):**
```json
1
```

---

### 16. Listar Expansiones

**GET** `/nodos/ExpansionPacks`

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "name": "PackUno",
    "description": "Descripción del pack",
    "distributor": "EA",
    "price": 25.5,
    "category": "RPG",
    "publicationDate": "2026-01-01",
    "language": "ES",
    "deleted": false,
    "cartDetails": []
  }
]
```

---

### 17. Obtener Expansión por ID

**GET** `/nodos/ExpansionPacks/{id}`

**Respuesta (200):**
```json
{
  "id": 1,
  "name": "PackUno",
  "description": "Descripción del pack",
  "distributor": "EA",
  "price": 25.5,
  "category": "RPG",
  "publicationDate": "2026-01-01",
  "language": "ES",
  "deleted": false,
  "cartDetails": []
}
```

---

### 18. Actualizar Expansión (Admin)

**PUT** `/nodos/ExpansionPacks/{id}`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

**Body:**
```json
{
  "name": "PackUno Plus",
  "description": "Descripción actualizada"
}
```

**Respuesta (200):**
```json
{
  "id": 1,
  "name": "PackUno Plus",
  "description": "Descripción actualizada",
  ...
}
```

**Error (500):**
```json
"Internal error: JSON parse error: Unexpected end-of-input in VALUE_STRING"
```

---

### 19. Eliminar Expansión (Admin)

**DELETE** `/nodos/ExpansionPacks/{id}`

**Headers:**
```
Authorization: Bearer <ADMIN_TOKEN>
```

**Respuesta (200):**
```json
"Expansion Pack deleted successfully"
```

---

## Carrito (Cart)

### 20. Obtener Carrito

**GET** `/nodos/cart`

**Headers:**
```
Authorization: Bearer <USER_TOKEN>
```

**Respuesta (200):**
```json
{
  "id": 1,
  "status": "activo",
  "user": {
    "id": 2,
    "name": "User Tester",
    "email": "usernodos@example.com",
    "username": "usernodos",
    "country": "Colombia"
  },
  "items": [
    {
      "id": 1,
      "expansionPack": {
        "id": 1,
        "name": "PackUno",
        "description": "Descripción",
        "price": 25.5
      },
      "platform": {
        "id": 1,
        "name": "Steam"
      }
    }
  ],
  "total": 25.5
}
```

---

### 21. Agregar al Carrito

**POST** `/nodos/cart/add`

**Headers:**
```
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer <USER_TOKEN>
```

**Body (form-data):**
```
expansionId=1
platformId=1
```

**Respuesta (200):**
```json
{
  "id": 1,
  "status": "activo",
  "items": [...],
  "total": 25.5
}
```

---

### 22. Remover del Carrito

**POST** `/nodos/cart/remove`

**Headers:**
```
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer <USER_TOKEN>
```

**Body (form-data):**
```
expansionId=1
```

**Respuesta (200):**
```json
{
  "id": 1,
  "status": "activo",
  "items": [],
  "total": 0.0
}
```

---

### 23. Vaciar Carrito

**POST** `/nodos/cart/clear`

**Headers:**
```
Authorization: Bearer <USER_TOKEN>
```

**Respuesta (200):** Sin contenido

---

## Compras (Buys)

### 24. Comprar desde Carrito

**POST** `/nodos/buys/purchase`

**Headers:**
```
Content-Type: text/plain
Authorization: Bearer <USER_TOKEN>
```

**Body:**
```
CARD
```

**Respuesta (200):**
```json
{
  "id": 1,
  "purchaseDate": "2026-03-30T21:25:09.503+00:00",
  "totalPrice": 25.5,
  "paymentMethod": "CARD",
  "status": "completado",
  "items": [
    {
      "id": 1,
      "quantity": 1,
      "expansionPack": {
        "id": 1,
        "name": "PackUno",
        "description": "Descripción",
        "price": 25.5
      },
      "platform": {
        "id": 1,
        "name": "Steam"
      }
    }
  ]
}
```

---

### 25. Compra Directa

**POST** `/nodos/buys/direct`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <USER_TOKEN>
```

**Body:**
```json
{
  "expansionId": 1,
  "platformId": 1,
  "paymentMethod": "CARD"
}
```

**Respuesta (200):**
```json
{
  "id": 2,
  "purchaseDate": "2026-03-30T21:25:10.031+00:00",
  "totalPrice": 25.5,
  "paymentMethod": "CARD",
  "status": "completado",
  "items": [...]
}
```

---

### 26. Listar Compras del Usuario

**GET** `/nodos/buys`

**Headers:**
```
Authorization: Bearer <USER_TOKEN>
```

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "purchaseDate": "2026-03-30T21:25:09.503+00:00",
    "totalPrice": 25.5,
    "paymentMethod": "CARD",
    "status": "completado",
    "items": [...]
  }
]
```

---

### 27. Obtener Compra por ID

**GET** `/nodos/buys/{id}`

**Headers:**
```
Authorization: Bearer <USER_TOKEN>
```

**Respuesta (200):**
```json
{
  "id": 1,
  "purchaseDate": "2026-03-30T21:25:09.503+00:00",
  "totalPrice": 25.5,
  "paymentMethod": "CARD",
  "status": "completado",
  "items": [...]
}
```

---

## Usuarios (Admin)

### 28. Listar Usuarios (Admin)

**GET** `/nodos/Users`

**Headers:**
```
Authorization: Bearer <ADMIN_TOKEN>
```

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "name": "Admin Tester",
    "firstName": "Admin",
    "lastName": "Tester",
    "email": "admin_nodos@example.com",
    "registrationDate": "2026-03-30T21:13:03.870+00:00",
    "role": "ROLE_ADMIN",
    "nickname": null,
    "country": "Colombia",
    "username": "admin_nodos",
    "password": "$2a$10$...",
    "deleted": false,
    "enabled": true,
    "accountNonExpired": true,
    "accountNonLocked": true,
    "credentialsNonExpired": true,
    "authorities": [{"authority": "ROLE_ADMIN"}]
  }
]
```

---

### 29. Obtener Usuario por ID

**GET** `/nodos/Users/{id}`

**Headers:**
```
Authorization: Bearer <ADMIN_TOKEN>
```

**Respuesta (200):**
```json
{
  "id": 1,
  "name": "Admin Tester",
  ...
}
```

---

### 30. Actualizar Usuario

**PUT** `/nodos/Users/{id}`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

**Body:**
```json
{
  "name": "Nombre Actualizado",
  "email": "email@ejemplo.com"
}
```

**Respuesta (200):**
```json
{
  "id": 1,
  "name": "Nombre Actualizado",
  "email": "email@ejemplo.com",
  ...
}
```

---

### 31. Eliminar Usuario

**DELETE** `/nodos/Users/{id}`

**Headers:**
```
Authorization: Bearer <ADMIN_TOKEN>
```

**Respuesta (200):**
```json
"User deleted successfully"
```

---

### 32. Actualizar Rol de Usuario

**PUT** `/nodos/Users/{id}/role`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

**Body:**
```json
"ROLE_ADMIN"
```

**Respuesta (200):**
```json
{
  "id": 1,
  "role": "ROLE_ADMIN",
  ...
}
```

---

## Errores Comunes

| Código | Descripción |
|--------|-------------|
| 400 | Validación de datos incorrecta |
| 401 | Token inválido o no proporcionado |
| 403 | Sin permisos suficientes |
| 500 | Error interno del servidor |

**Ejemplos de errores:**

- **401 - Token no válido:**
```json
{"error": "Token invalidated"}
```

- **500 - Contenido no encontrado:**
```json
"Internal error: Content not found"
```

- **500 - Error de parseo JSON:**
```json
"Internal error: JSON parse error: Unexpected end-of-input in VALUE_STRING"
```

---

## Notas

1. Los endpoints de contenido público (GET) no requieren autenticación.
2. Los endpoints de modificación (POST, PUT, DELETE) requieren rol ADMIN.
3. Los endpoints de carrito y compras requieren autenticación (USER o ADMIN).
4. Después de una compra, se crea un nuevo carrito activo automáticamente.
5. El logout invalida el token JWT utilizado.
