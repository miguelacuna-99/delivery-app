# comercio-service

Dueño de dos cosas: la **ficha del comercio con su suscripción a la plataforma** y el **catálogo de productos** que el cliente ve antes de pedir.

Ojo con la palabra "pago" aquí: la suscripción es lo que el comercio le paga a la plataforma por operar, y no tiene nada que ver con el cobro de un pedido (eso es `pago-service`).

- **Puerto:** 8082 · **Base de datos:** `comercio_db` · **Paquete:** `es.delivery.manager.comercio`

## API REST

### Comercio y suscripción

| Método | Ruta | Protección | Qué hace |
|---|---|---|---|
| `POST` | `/api/comercios` | `X-Platform-Key` | Alta de comercio. Nace `activo` con suscripción `ACTIVA` y vencimiento según el `plan` |
| `GET` | `/api/comercios` | pública | Comercios **operativos** (los suspendidos no salen) |
| `GET` | `/api/comercios/{id}` | pública | Ficha de un comercio |
| `GET` | `/api/comercios/me` | Bearer (usuario de comercio) | Mi comercio, según el `comercioId` del token |
| `PUT` | `/api/comercios/me` | Bearer `ROOT` o `ADMIN` | Edita nombre, dirección, teléfono y email. **No** toca CIF ni suscripción |
| `POST` | `/api/comercios/{id}/suscripcion/suspender` | `X-Platform-Key` | Suspende por impago → publica `comercio.suspendido` |
| `POST` | `/api/comercios/{id}/suscripcion/renovar` | `X-Platform-Key` | Renueva con un plan → si estaba suspendida, publica `comercio.reactivado` |

### Catálogo

| Método | Ruta | Protección | Qué hace |
|---|---|---|---|
| `GET` | `/api/comercios/{comercioId}/productos` | pública | Catálogo de un comercio |
| `POST` | `/api/productos` | Bearer `ROOT` o `ADMIN` | Crea producto. El `comercioId` sale del token |
| `PUT` | `/api/productos/{id}` | Bearer `ROOT` o `ADMIN` | Edita nombre, descripción, ingredientes, imagen, precio y disponibilidad |
| `DELETE` | `/api/productos/{id}` | Bearer `ROOT` o `ADMIN` | Elimina producto (204) |

Errores: 409 CIF duplicado · 403 tipo de usuario sin permiso o `X-Platform-Key` inválida · 404 comercio/producto no encontrado · 401 token inválido.

**Un producto de otro comercio responde 404, no 403.** Es intencionado: un 403 confirmaría que ese id existe.

## Eventos

**Publica** en `delivery.exchange`:

| Routing key | Cuándo | Mensaje |
|---|---|---|
| `comercio.suspendido` | La plataforma suspende una suscripción activa | `ComercioEventMessage` |
| `comercio.reactivado` | Se renueva una suscripción que estaba suspendida | `ComercioEventMessage` |

**Consume:** nada.

Los escucha `pedido-service`, que mantiene una réplica local del estado y **deja de admitir pedidos** de un comercio suspendido hasta que se reactive.

`comercio-service` también sirve el catálogo a `pedido-service`: en cada checkout consulta `GET /api/comercios/{id}/productos` para revalidar precios y disponibilidad, de modo que el precio que se cobra sale siempre de aquí y nunca del carrito del cliente.

## Dependencias

**Maven** (Spring Boot 3.3.0, Java 21): `spring-boot-starter-web`, `spring-boot-starter-data-mongodb`, `spring-boot-starter-amqp`, `delivery-contracts` 1.0.0, Lombok 1.18.30, MapStruct 1.5.5.Final, `spring-boot-starter-test`.

**Infraestructura:** MongoDB y RabbitMQ.

**Otros servicios:** `auth-service` — todo endpoint con Bearer se valida contra `POST /auth/validate`. Si auth está caído, los endpoints públicos siguen funcionando; los autenticados no.

**Quién depende de este servicio:** `pedido-service` consulta el catálogo en cada checkout, así que si `comercio-service` está caído no se pueden crear pedidos.

## Configuración

| Variable | Por defecto | Qué es |
|---|---|---|
| `MONGO_USERNAME` / `MONGO_PASSWORD` | — (obligatorias) | Credenciales de Mongo |
| `MONGO_HOST` / `MONGO_PORT` | `localhost` / `27017` | Dónde está Mongo |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | Dónde está RabbitMQ |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | — (obligatorias) | Credenciales de RabbitMQ |
| `AUTH_SERVICE_URL` | `http://localhost:8081` | Base de `auth-service` |
| `PLATFORM_API_KEY` | `dev-platform-key-cambiar` | Valor esperado en `X-Platform-Key` |

## Modelo de datos (`comercio_db`)

- **`comercios`** — `nombre`, `cif` (único), `direccion`, `telefono`, `email`, `activo`, `plan`, `estadoSuscripcion`, `fechaInicioSuscripcion`, `fechaFinSuscripcion`
- **`productos`** — `comercioId` (indexado), `nombre`, `descripcion`, `ingredientes`, `imagenUrl`, `precio`, `disponible`

## Reglas de la suscripción

- **`Comercio.esOperativo()`** es el único sitio donde vive la regla: `ACTIVA` y `EN_GRACIA` operan, `SUSPENDIDA` no. El listado público filtra por ahí.
- **Renovar respeta lo que queda**: si la suscripción aún no ha vencido, el nuevo periodo se suma desde `fechaFinSuscripcion`; si ya venció, cuenta desde hoy. Nadie pierde días por renovar antes de tiempo.
- **Suspender es idempotente**: sobre una suscripción ya suspendida no persiste ni publica nada.
- **`EN_GRACIA` está modelado pero nadie lo activa todavía**: no hay proceso programado que degrade `ACTIVA → EN_GRACIA → SUSPENDIDA` al vencer. Hoy suspender es una acción explícita de la plataforma.
- **Suspender tiene efecto real en los pedidos**: el evento llega a `pedido-service`, que rechaza con 409 los checkouts de ese comercio hasta que se reactive.
- **El comercio no puede tocar su propia suscripción.** Ni ROOT ni ADMIN: solo la plataforma con `X-Platform-Key`.

## Construir y ejecutar

```bash
mvn -f delivery-contracts/pom.xml install -DskipTests   # una vez
mvn -f comercio-service/pom.xml clean package
mvn -f comercio-service/pom.xml test
mvn -f comercio-service/pom.xml spring-boot:run
```

Colección Postman: `postman/comercio-service.postman_collection.json`.
