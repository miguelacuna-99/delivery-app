# auth-service

Identidad de toda la plataforma: emite los JWT y los valida para los demás servicios. Gestiona dos poblaciones distintas — **usuarios de comercio** (`ROOT`, `ADMIN`, `PERSONAL`, `REPARTIDOR`) y **clientes** (`CLIENTE`) — y el ciclo de contraseñas olvidadas. Cada web se vende a un único comercio, así que todo usuario, incluido el cliente, lleva `comercioId`.

Es el único servicio que **no** habla con RabbitMQ: todo su trato con los demás es HTTP síncrono a través de `POST /auth/validate`.

- **Puerto:** 8081 · **Base de datos:** `auth_db` · **Paquete:** `es.delivery.manager.auth`

## API REST

| Método | Ruta | Protección | Qué hace |
|---|---|---|---|
| `POST` | `/auth/login` | pública | Login de usuarios de comercio y de clientes. Devuelve `token`, `tipo` y `mustChangePassword` |
| `POST` | `/auth/validate` | pública (la llaman los servicios) | Valida el token. Siempre 200: `valid=true` + claims, o `valid=false` |
| `POST` | `/auth/password/forgot` | pública | Envía la URL de reseteo al correo. **Siempre 204**, exista o no el mail |
| `POST` | `/auth/password/reset` | pública | Fija la nueva contraseña. Token de un solo uso; 400 si ya se usó o caducó |
| `POST` | `/api/clientes/registro` | pública | Alta de cliente. `comercioId` obligatorio en el body: el front, dedicado a un único comercio, lo obtiene antes con `GET /api/comercios` |
| `PUT` | `/api/clientes/me/contacto` | Bearer `CLIENTE` | El cliente edita mail, dirección y teléfono (solo los campos informados) |
| `POST` | `/api/usuarios/root` | cabecera `X-Platform-Key` | La plataforma provisiona el ROOT de un comercio. Dispara el correo de bienvenida con la URL de reseteo |
| `POST` | `/api/usuarios` | Bearer `ROOT` o `ADMIN` | Crea usuarios del comercio. El `comercioId` **siempre** sale del token, nunca del body |
| `GET` | `/api/usuarios` | Bearer `ROOT` | Lista los usuarios de mi comercio |
| `DELETE` | `/api/usuarios/{id}` | Bearer `ROOT` | Elimina un usuario de mi comercio (404 si es de otro); 400 si intento eliminarme a mí mismo |

Matriz de creación de usuarios: `ROOT` crea cualquier tipo de usuario de comercio (incluido otro ROOT); `ADMIN` solo `PERSONAL` y `REPARTIDOR`; `CLIENTE` no se crea nunca por aquí, se registra él mismo.

Códigos de error (`GlobalExceptionHandler`): 409 username duplicado · 401 credenciales inválidas · 400 token de reseteo inválido o caducado, o `comercioId` ausente en el registro de cliente · 403 operación no permitida · 404 cliente no encontrado.

### El JWT

Claims: `userId`, `username`, `comercioId` y `tipo`. Firmado HS256, caducidad 24 h.

A diferencia del resto de servicios, aquí no hay `JwtAuthInterceptor`: los dos endpoints con Bearer validan el token en el propio controlador (`validateBearer`), porque este servicio *es* el validador. Tampoco hay filter chain de Spring Security — solo se usa `spring-security-crypto` para BCrypt.

## Eventos

Ninguno. No publica ni consume nada de RabbitMQ.

## Dependencias

**Maven** (Spring Boot 3.3.0, Java 21):

| Dependencia | Para qué |
|---|---|
| `spring-boot-starter-web` | API REST |
| `spring-boot-starter-data-mongodb` | Persistencia |
| `spring-security-crypto` | BCrypt, y **solo** BCrypt |
| `jjwt-api` / `jjwt-impl` / `jjwt-jackson` 0.12.3 | Firmar y verificar los JWT |
| `delivery-contracts` 1.0.0 | `TipoUsuario` |
| Lombok 1.18.30 · MapStruct 1.5.5.Final | Boilerplate y mapeos |
| `spring-boot-starter-test` | JUnit 5 + Mockito + AssertJ |

**Infraestructura:** MongoDB. No necesita RabbitMQ.

**Otros servicios:** ninguno. Es la hoja del grafo de dependencias — arranca solo.

## Configuración

| Variable / property | Por defecto | Qué es |
|---|---|---|
| `MONGO_USERNAME` / `MONGO_PASSWORD` | — (obligatorias) | Credenciales de Mongo |
| `MONGO_HOST` / `MONGO_PORT` | `localhost` / `27017` | Dónde está Mongo |
| `JWT_SECRET` | clave de desarrollo | Secreto HS256, **mínimo 32 caracteres**. Cámbialo en producción |
| `jwt.expiration-ms` | `86400000` (24 h) | Vigencia del token |
| `PLATFORM_API_KEY` | `dev-platform-key-cambiar` | Valor esperado en `X-Platform-Key` |
| `RESET_BASE_URL` | `http://localhost:3000/reset-password` | Base de la URL que se manda por correo |
| `app.reset-token-expiration-min` | `30` | Caducidad del token de reseteo |

## Modelo de datos (`auth_db`)

- **`usuarios`** — `comercioId`, `username` (único), `passwordHash` (BCrypt), `mail`, `telefono`, `tipo`, `mustChangePassword`
- **`clientes`** — `comercioId` (indexado), `username` (único), `passwordHash`, `mail`, `direccionDomicilio`, `telefono`
- **`password_reset_tokens`** — `userId`, `mail`, **hash SHA-256** del token (nunca el token en claro), `expiresAt`, `usado`

## Detalles que conviene saber

- **`/auth/password/forgot` siempre responde 204**, exista o no el correo. Es deliberado: si distinguiera, cualquiera podría enumerar las cuentas registradas.
- **Del token de reseteo solo se guarda el hash SHA-256.** Quien lea la base de datos no puede usarlos.
- **El correo es un mock**: `LoggingEmailAdapter` implementa `EmailPort` escribiendo el mensaje en el log. Para ver la URL de reseteo, mira los logs del contenedor. Sustituirlo por SMTP real es cambiar ese único adaptador.
- **El ROOT nace con `mustChangePassword = true`** y sin contraseña utilizable de verdad: el alta dispara el flujo de reseteo por correo para que la fije él.
- **El `comercioId` del registro de cliente no se valida contra `comercio-service`.** auth-service sigue sin llamar a otros servicios; se confía en el valor que manda el front (igual que `ProvisionRootRequest` hoy). Solo se exige que no venga vacío.

## Construir y ejecutar

```bash
mvn -f delivery-contracts/pom.xml install -DskipTests   # una vez
mvn -f auth-service/pom.xml clean package
mvn -f auth-service/pom.xml test
mvn -f auth-service/pom.xml spring-boot:run
```

Colección Postman: `postman/auth-service.postman_collection.json`.
