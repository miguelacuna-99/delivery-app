# fidelidad-service

Programa de fidelización: **cupones de descuento** que crea el ADMIN del comercio y **puntos** que el cliente gana pagando y canjea como descuento.

Tiene dos caras: una API REST (gestión de cupones y consulta de puntos, más dos endpoints internos que usa `pedido-service` en el checkout) y un consumidor de eventos que mantiene el saldo al día conforme el pedido avanza.

- **Puerto:** 8085 · **Base de datos:** `fidelidad_db` · **Paquete:** `es.delivery.manager.fidelidad`

## API REST

### Cupones

| Método | Ruta | Protección | Qué hace |
|---|---|---|---|
| `POST` | `/api/cupones` | Bearer `ADMIN` | Crea un cupón `ACTIVO`. El `comercioId` sale del token |
| `POST` | `/api/cupones/{id}/anular` | Bearer `ADMIN` | Pasa el cupón a `ANULADO` |
| `GET` | `/api/cupones` | Bearer `ROOT` o `ADMIN` | Cupones de mi comercio |
| `GET` | `/api/cupones/validar?codigo=&comercioId=&clienteId=` | **interno**, `X-Service-Key` | ¿Usable? → `{usable, porcentajeDescuento}` |

**Solo el `ADMIN` crea y anula cupones** — el `ROOT` puede consultarlos, pero no gestionarlos. Es una decisión explícita de diseño, no un olvido.

### Puntos

| Método | Ruta | Protección | Qué hace |
|---|---|---|---|
| `GET` | `/api/puntos/me` | Bearer `CLIENTE` | Mi saldo y el historial de movimientos, para el comercio de mi token |
| `GET` | `/api/puntos/{clienteId}/saldo?comercioId=` | **interno**, `X-Service-Key` | Saldo de un cliente en ese comercio → `{clienteId, saldo}` |

Errores: 409 código de cupón duplicado · 403 tipo de usuario sin permiso · 404 cupón inexistente o de otro comercio · 401 token o clave de servicio inválidos.

### Endpoints internos y la clave de servicio

Los dos endpoints marcados como internos no los llama un usuario, sino `pedido-service` durante el checkout: no llevan JWT porque no hay ninguna sesión detrás. Se protegen con una **clave compartida de servicio a servicio** en la cabecera `X-Service-Key` (`ServiceKeyInterceptor`), que debe coincidir con el `SERVICE_API_KEY` de `pedido-service`. Sin ella respondían a cualquiera que alcanzase el puerto 8085, exponiendo el saldo de puntos de cualquier cliente cuyo id se conociera.

## Eventos

**Consume:**

| Cola | Routing keys | Efecto |
|---|---|---|
| `fidelidad.pedido.queue` | `pedido.*` | `pedido.creado` reserva · `pedido.aceptado` consolida · `pedido.rechazado` y `pedido.cancelado` devuelven · `pedido.entregado` sin efecto |
| `fidelidad.pago.queue` | `pago.completado` | Otorga puntos por el gasto |
| `fidelidad.devolucion.queue` | `devolucion.completada` | Retira los puntos que ese pedido otorgó |

**Publica:** nada.

> **`pago.fallido` no se escucha aquí.** Sus efectos llegan como `pedido.cancelado`, que sí trae el código del cupón y los puntos aplicados — datos del pedido que `PagoEventMessage` no lleva. Sin ese rodeo se podían devolver los puntos pero no liberar el uso del cupón.

> El binding de la cola de pagos es **explícito**, no `pago.*`. Con el wildcard entraría también `pago.solicitado`, que viaja como `PedidoEventMessage`: su campo `estado` lleva un `EstadoPedido` (`ACEPTADO`) que no existe en `EstadoPago`, así que la deserialización al `PagoEventMessage` del listener falla y el mensaje se descarta con error. El wildcard `pedido.*` sí es seguro porque todas sus claves llevan el mismo mensaje.

## Cómo funcionan los puntos

**Conversión:** se gana **1 punto por cada euro** del total cobrado (se trunca hacia abajo: 18,80 € → 18 puntos). Se canjea a razón de **1 punto = 0,01 €** de descuento.

**Ciclo reserva → consolidación:**

| Momento | Qué pasa |
|---|---|
| `pedido.creado` | **Reserva**: se descuentan los puntos aplicados (movimiento `CANJEADO`, negativo) y se suma 1 al contador de usos del cupón para ese cliente |
| `pedido.aceptado` | **Consolida**: sin efecto económico, la reserva ya se aplicó. Solo queda trazado en el log |
| `pedido.rechazado` | **Devuelve**: movimiento `DEVUELTO` y se resta 1 al contador del cupón |
| `pedido.cancelado` | Igual que el rechazo: pago denegado o plazo de pago vencido |
| `pago.completado` | Movimiento `GANADO` con los puntos del gasto |
| `devolucion.completada` | Movimiento `RETIRADO` (negativo) por los puntos que ese pedido otorgó |

**Todo es idempotente.** Antes de devolver, otorgar o retirar se mira si ya existe un movimiento de ese tipo para ese `pedidoId`; si lo hay, el evento se ignora. Importa porque RabbitMQ garantiza *al menos una* entrega, no exactamente una: sin esto, un reenvío abonaría los puntos dos veces.

## Cómo funcionan los cupones

- **X usos por cliente**: cada cupón lleva su lista `usos[{clienteId, contador}]`. Agotados los usos, deja de ser usable **para ese cliente**, no para los demás.
- **`Cupon.esUsable(ahora)`** concentra la validación: `ACTIVO` y no caducado.
- **Caducidad perezosa**: al validar un cupón `ACTIVO` cuya fecha ya pasó, se persiste como `CADUCADO` sobre la marcha. No hace falta un proceso programado que barra la colección.
- Un cupón de **otro comercio** simplemente no es usable, aunque el código exista.

## Dependencias

**Maven** (Spring Boot 3.3.0, Java 21): `spring-boot-starter-web`, `spring-boot-starter-data-mongodb`, `spring-boot-starter-amqp`, `delivery-contracts` 1.0.0, Lombok 1.18.30, MapStruct 1.5.5.Final, `spring-boot-starter-test`.

**Infraestructura:** MongoDB y RabbitMQ.

**Otros servicios:** `auth-service` para validar los Bearer. A su vez, **`pedido-service` depende de este servicio** en el checkout: si fidelidad está caído, los checkouts con cupón o puntos fallan.

## Configuración

| Variable | Por defecto | Qué es |
|---|---|---|
| `MONGO_USERNAME` / `MONGO_PASSWORD` | — (obligatorias) | Credenciales de Mongo |
| `MONGO_HOST` / `MONGO_PORT` | `localhost` / `27017` | Dónde está Mongo |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | Dónde está RabbitMQ |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | — (obligatorias) | Credenciales de RabbitMQ |
| `AUTH_SERVICE_URL` | `http://localhost:8081` | Base de `auth-service` |
| `SERVICE_API_KEY` | `dev-service-key-cambiar` | Clave que exigen los endpoints internos. Debe coincidir con la de `pedido-service` |

La conversión de puntos (1 punto = 0,01 €) está hoy como constante en el código de `pedido-service` (`CheckoutService.VALOR_PUNTO`) y el "1 punto por euro" en `FidelidadEventService`. Si se quiere ajustar por comercio, es lo primero que habría que externalizar.

## Modelo de datos (`fidelidad_db`)

- **`cupones`** — `comercioId` (indexado), `codigo` (único), `porcentajeDescuento`, `usosMaximosPorUsuario`, `usos[]`, `estado`, `fechaCaducidad`
- **`cuentas_puntos`** — `clienteId` + `comercioId` (índice único compuesto), `saldo`, `movimientos[{pedidoId, tipo, puntos, fecha}]`

**El saldo de puntos es por (cliente, comercio), no global.** Coherente con que cada cliente esté atado a un único comercio desde su registro (`auth-service`): un mismo `clienteId` tiene una `CuentaPuntos` distinta por cada comercio en el que haya pagado.

## Limitaciones conocidas

1. **La conversión de puntos es global**, no por comercio: está como constante en el código.
2. **La clave de servicio es un secreto compartido en configuración.** Suficiente para separar el tráfico interno del externo, pero no sustituye a mTLS o a tokens de servicio firmados si algún día los servicios salen de la red privada.

## Construir y ejecutar

```bash
mvn -f delivery-contracts/pom.xml install -DskipTests   # una vez
mvn -f fidelidad-service/pom.xml clean package
mvn -f fidelidad-service/pom.xml test
mvn -f fidelidad-service/pom.xml spring-boot:run
```

Colección Postman: `postman/fidelidad-service.postman_collection.json`.
