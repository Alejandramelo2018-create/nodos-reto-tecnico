# Guía de Pruebas para Autenticación (Login/Registro y OAuth2)

Este documento explica cómo probar los diferentes métodos de autenticación disponibles en la aplicación:

- Registro de usuario estándar
- Login de usuario estándar (username/password)
- Login con OAuth2 (Google y Meta/Facebook)

## Requisitos Previos

1. **Java 17** instalado y configurado como `JAVA_HOME`
2. **PostgreSQL** en ejecución (puerto 5432 por defecto)
3. Base de datos `nodos_db` creada y esquema `reto` existente (la aplicación lo crea automáticamente en arranque)
4. Archivo `.env` en la raíz del proyecto con las variables necesarias (ver sección de configuración)

## Paso 1: Iniciar la Aplicación

Desde la raíz del proyecto, ejecuta:

```bash
export JWT_SECRET=your_secure_jwt_secret_key_min_32_chars_1234567890
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
./mvnw spring-boot:run
```

La aplicación iniciará en el puerto **8081** (configurado en `.env` o `application.properties`).  
Espera hasta ver en la consola algo como:

```
Started RetoTecnicoApplication in X.XXX seconds (process running for Y.YYY)
```

## Paso 2: Probar Registro de Usuario (Endpoint Standard)

### Request
```bash
curl -X POST http://localhost:8081/auth/register \
     -H "Content-Type: application/json" \
     -d '{
           "username": "testuser22",
           "password": "Testpass1232!",
           "email": "test@example.com",
           "name": "Test User",
           "firstName": "Test",
           "lastName": "User",
           "nickname": "testy",
           "country": "USA",
           "role": "USER"
         }'
```

### Respuesta Esperada (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTc3MzcyOTMwOSwiZXhwIjoxNzczODE1NzA5fQ.3fZQm5Rg-rykC79RuBS9owqjQFLiFjyViLkoCmbD6us"
}
```

> **Nota**: La contraseña debe contener al menos una letra mayúscula, una minúscula, un número y un carácter especial.

## Paso 3: Probar Login de Usuario (Endpoint Standard)

### Request
```bash
curl -X POST http://localhost:8081/auth/login \
     -H "Content-Type: application/json" \
     -d '{
           "username": "testuser",
           "password": "Testpass123!"
         }'
```

### Respuesta Esperada (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTc3MzcyOTMxOSwiZXhwIjoxNzczODE1NzE5fQ.lRz36w7Mods45lMSRzn_L2kSgPnmub9AfPkYmU2OHUI"
}
```

Este token JWT debe ser usado en el header `Authorization: Bearer <token>` para acceder a endpoints protegidos (aunque actualmente todos los endpoints fuera de `/auth/**` requieren autenticación).

## Paso 4: Probar OAuth2 con Google

### Paso 4.1: Iniciar el flujo de autorización
```bash
curl -v http://localhost:8081/oauth2/authorization/google
```

### Respuesta Esperada
- Código de estado **302 Found**
- Header `Location` con una URL de redireccionamiento a `https://accounts.google.com/o/oauth2/v2/auth?...`

### Paso 4.2: Completar el flujo en el navegador (opcional para prueba manual)
1. Copia la URL del header `Location` de la respuesta anterior.
2. Pégala en tu navegador.
3. Inicia sesión con tu cuenta de Google y concede los permisos solicitados.
4. Google redirigirá de vuelta a `http://localhost:8081/login/oauth2/code/google?code=...&state=...`
5. Spring Security interceptará esa llamada, obtendrá los datos del usuario y redirigirá a `/auth/oauth2/success`.

### Paso 4.3: Verificar el endpoint de éxito (si ya completaste el flujo)
```bash
curl -v http://localhost:8081/auth/oauth2/success
```
> **Nota**: Este endpoint solo devuelve una respuesta válida si se accede después de un flujo OAuth2 exitoso (por lo que normalmente lo invocas indirectamente mediante el redirect de Google).

El controlador `AuthController` crea un JWT token usando el email (o nombre) del usuario autenticado por OAuth2 y lo devuelve en una `OAuth2Response`:

```json
{
  "token": "jwt_generado_aqui",
  "message": "OAuth2 login successful",
  "provider": "google",
  "email": "usuario@gmail.com",
  "name": "Nombre Usuario"
}
```

## Paso 5: Probar OAuth2 con Meta/Facebook

El proceso es idéntico al de Google, pero usando el proveedor `meta`.

### Paso 5.1: Iniciar el flujo de autorización
```bash
curl -v http://localhost:8081/oauth2/authorization/meta
```

### Respuesta Esperada
- Código de estado **302 Found**
- Header `Location` con una URL de redireccionamiento a `https://www.facebook.com/v18.0/dialog/oauth?...`

### Paso 5.2: Completar el flujo en el navegador
1. Copia la URL del header `Location`.
2. Pégala en tu navegador.
3. Inicia sesión con tu cuenta de Facebook y concede los permisos solicitados (`public_profile, email`).
4. Facebook redirigirá de vuelta a `http://localhost:8081/login/oauth2/code/meta?code=...&state=...`.
5. Spring Security procesará la respuesta y redirigirá a `/auth/oauth2/success`.

### Paso 5.3: Verificar el endpoint de éxito
```bash
curl -v http://localhost:8081/auth/oauth2/success
```
Respuesta similar a la de Google, pero con `provider`: `"meta"`.

## Paso 6: Usar el JWT Token en Requests Protegidas

Una vez que tienes un token (ya sea del login estándar o de OAuth2), puedes usarlo para acceder a cualquier endpoint que requiera autenticación (actualmente todos excepto `/auth/**`, `/oauth2/**`, `/login**` y `/error**`).

### Ejemplo (endpoint ficticio protegido)
```bash
curl -v http://localhost:8081/algún/endpoint/protegido \
     -H "Authorization: Bearer <tu_token_jwt_aqui>"
```

Si el token es válido, la request será procesada; si no, recibirás `401 Unauthorized`.

## Notas Adicionales

- **Variables de Entorno**: Asegúrate de que el archivo `.env` contenga:
  ```
  GOOGLE_CLIENT_ID=tu_google_client_id
  GOOGLE_CLIENT_SECRET=tu_google_client_secret
  META_CLIENT_ID=tu_facebook_app_id
  META_CLIENT_SECRET=tu_facebook_app_secret
  JWT_SECRET=your_secure_jwt_secret_key_min_32_chars_1234567890
  DB_PASSWORD=tu_contraseña_de_postgres
  ```
  (Los valores por defecto ya están en el archivo si no los modificaste.)

- **Esquema de Base de Datos**: La propiedad `spring.jpa.properties.hibernate.default_schema=reto` en `application.properties` obliga a que las tablas se creen en el esquema `reto`. Si al iniciar la aplicación ves errores de `"schema "reto" does not exist"`, asegúrate de haber creado el esquema en PostgreSQL:
  ```sql
  CREATE SCHEMA IF NOT EXISTS reto;
  ```
  (La aplicación lo intenta crear automáticamente, pero puede requerir permisos suficientes.)

- **Cierre de la Aplicación**: Para detener la aplicación, presiona `Ctrl + C` en la terminal donde la ejecutaste, o ejecuta:
  ```bash
  pkill -f mvnw
  ```

## Resumen de Endpoints de Autenticación

| Método | URL | Descripción |
|--------|-----|-------------|
| POST | `/auth/register` | Registro de nuevo usuario (username/password) |
| POST | `/auth/login` | Login estándar (username/password) → devuelve JWT |
| GET | `/oauth2/authorization/google` | Inicia flujo OAuth2 con Google |
| GET | `/oauth2/authorization/meta` | Inicia flujo OAuth2 con Meta/Facebook |
| GET | `/auth/oauth2/success` | Endpoint de éxito tras OAuth2 (devuelve JWT y datos del usuario) |

Con esto ya puedes probar todos los modos de autenticación disponibles en la aplicación.