# notificacion-service

Las dos bandejas que el comercio mira todo el día: **PEDIDOS PENDIENTES** (hay un pedido esperando respuesta) y **PEDIDOS PAGADOS** (ya está cobrado, a prepararlo).

Es el servicio más simple del sistema: escucha dos eventos, guarda una notificación y la sirve por REST hasta que alguien la marca como leída.

- **Puerto:** 8086 · **Base de datos:** `notificacion_db` · **Paquete:** `es.delivery.manager.notificacion`

## API REST

| Método | Ruta | Protección | Qué hace |
|---|---|---|---|
| `GET` | `/api/notificaciones?tipo=PEDIDO_PENDIENTE` | Bearer `ROOT`/`ADMIN`/`PERSONAL` | Notificaciones **sin leer** de mi comercio, por tipo |
| `POST` | `/api/notificaciones/{id}/leida` | Bearer `ROOT`/`ADMIN`/`PERSONAL` | Marca una notificación como leída |

`tipo` acepta `PEDIDO_PENDIENTE` o `PEDIDO_PAGADO`. El listado devuelve **solo las no leídas**: es una bandeja de trabajo, no un histórico.

`REPARTIDOR` y `CLIENTE` reciben 403. Una notificación de otro comercio responde 404.

## Eventos

**Consume:**

| Cola | Routing key | Mensaje | Crea |
|---|---|---|---|
| `notificacion.pedido-creado.queue` | `pedido.creado` | `PedidoEventMessage` | `PEDIDO_PENDIENTE` con `pedidoId` y `numeroPedido` |
| `notificacion.pago-completado.queue` | `pago.completado` | `PagoEventMessage` | `PEDIDO_PAGADO` con `pedidoId` |

**Publica:** nada.

Las notificaciones de tipo `PEDIDO_PAGADO` van **sin `numeroPedido`** (queda `null`): `PagoEventMessage` no lo lleva, porque el número de pedido es un dato del pedido, no del cobro. Si la interfaz lo necesita, o lo resuelve contra `pedido-service`, o se añade el campo al contrato de pago.

## Dependencias

**Maven** (Spring Boot 3.3.0, Java 21): `spring-boot-starter-web`, `spring-boot-starter-data-mongodb`, `spring-boot-starter-amqp`, `delivery-contracts` 1.0.0, Lombok 1.18.30, MapStruct 1.5.5.Final, `spring-boot-starter-test`.

**Infraestructura:** MongoDB y RabbitMQ.

**Otros servicios:** `auth-service` para validar los Bearer. Nadie depende de este servicio: es una hoja del sistema, y si se cae, el flujo del pedido sigue funcionando (el comercio se queda sin avisos, pero los pedidos entran igual).

## Configuración

| Variable | Por defecto | Qué es |
|---|---|---|
| `MONGO_USERNAME` / `MONGO_PASSWORD` | — (obligatorias) | Credenciales de Mongo |
| `MONGO_HOST` / `MONGO_PORT` | `localhost` / `27017` | Dónde está Mongo |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | Dónde está RabbitMQ |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | — (obligatorias) | Credenciales de RabbitMQ |
| `AUTH_SERVICE_URL` | `http://localhost:8081` | Base de `auth-service` |

## Modelo de datos (`notificacion_db`)

- **`notificaciones`** — `comercioId` (indexado), `tipo`, `pedidoId`, `numeroPedido`, `leida`, `fecha`

## Posibles siguientes pasos

- **Push real.** Hoy la bandeja es *pull*: el frontal pregunta. Un WebSocket o SSE por comercio evitaría el sondeo.
- **Más eventos.** `pedido.rechazado`, `pedido.entregado` o `devolucion.completada` no generan notificación; añadirlos es un binding y un caso en el listener.
- **Histórico.** Ahora solo se consultan las no leídas; falta un endpoint paginado para ver todo lo pasado.

## Construir y ejecutar

```bash
mvn -f delivery-contracts/pom.xml install -DskipTests   # una vez
mvn -f notificacion-service/pom.xml clean package
mvn -f notificacion-service/pom.xml test
mvn -f notificacion-service/pom.xml spring-boot:run
```

Colección Postman: `postman/notificacion-service.postman_collection.json`.
