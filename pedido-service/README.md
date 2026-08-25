# pedido-service

El corazón del sistema: carrito del cliente, creación del pedido y la máquina de estados completa. Es quien más eventos publica y quien orquesta el cobro y la devolución apoyándose en `pago-service`.

- **Puerto:** 8083 · **Base de datos:** `pedido_db` · **Paquete:** `es.delivery.manager.pedido`

## API REST

### Carrito (solo `CLIENTE`)

| Método | Ruta | Qué hace |
|---|---|---|
| `GET` | `/api/carrito` | Mi carrito. Si no hay, devuelve uno vacío en lugar de 404 |
| `PUT` | `/api/carrito` | Reemplaza `items[]` y `puntosAplicados`. El `comercioId` no sale del body: sale del token del cliente, que queda atado a un único comercio desde su registro |
| `POST` | `/api/carrito/cupon` | Valida el código contra `fidelidad-service` (existe, es de mi comercio y no está agotado/caducado) y lo fija en el carrito. 400 si no es usable |
| `DELETE` | `/api/carrito/cupon` | Quita el cupón aplicado |
| `DELETE` | `/api/carrito` | Vacía el carrito (204) |
| `POST` | `/api/carrito/checkout` | Convierte el carrito en pedido `PENDIENTE` (201) |

Un carrito es siempre **de un único comercio**: cambiar de comercio es reemplazarlo entero.

**El cupón solo se fija/quita vía `POST`/`DELETE /api/carrito/cupon`, nunca por el `PUT` de items.** Un `PUT /api/carrito` conserva siempre el `codigoCupon` que ya estuviera validado y guardado, ignorando cualquier valor que llegue en el body — así no se puede colar un código sin pasar por la validación contra `fidelidad-service`.

En el checkout, el precio, el nombre, los ingredientes y la imagen de cada item **se toman del catálogo de `comercio-service`**, no del carrito: del carrito solo se respeta qué producto y qué cantidad. Ver *Integridad del checkout* más abajo.

### Pedidos

| Método | Ruta | Protección | Qué hace |
|---|---|---|---|
| `GET` | `/api/pedidos/me` | Bearer `CLIENTE` | Mi historial de pedidos |
| `GET` | `/api/pedidos?estado=PENDIENTE` | Bearer `ROOT`/`ADMIN`/`PERSONAL` | Pedidos del comercio por estado |
| `POST` | `/api/pedidos/{id}/aceptar` | Bearer `ROOT`/`ADMIN`/`PERSONAL` | Acepta fijando `tiempoEstimadoMin` |
| `POST` | `/api/pedidos/{id}/pagar` | Bearer `CLIENTE` | El propio cliente dispara el cobro de su pedido `ACEPTADO`, con `tarjetaId` |
| `POST` | `/api/pedidos/{id}/rechazar` | Bearer `ROOT`/`ADMIN`/`PERSONAL` | Rechaza con `mensaje` opcional |
| `POST` | `/api/pedidos/entregar` | Bearer `REPARTIDOR` | Marca `ENTREGADO` a partir del `numeroPedido` que dicta el cliente |
| `POST` | `/api/pedidos/{id}/anular` | Bearer `ROOT` o `ADMIN` | Anula un pedido ya pagado con `motivo` → pide la devolución |

Errores: 404 pedido inexistente **o de otro comercio** · 400 transición inválida, carrito vacío, cupón no usable, puntos insuficientes, producto no disponible o cantidad no positiva · **409 precio desactualizado o comercio suspendido** · 403 tipo de usuario sin permiso · 401 token inválido.

## Máquina de estados

```
                  ┌─ RECHAZADO
PENDIENTE ────────┤
                  └─ ACEPTADO ──┬─ PAGADO ──┬─ ENTREGADO
                                │           └─ PENDIENTE_DEVOLUCION ── DEVUELTO
                                └─ CANCELADO
```

| De | A | Quién lo provoca |
|---|---|---|
| — | `PENDIENTE` | El cliente hace checkout |
| `PENDIENTE` | `ACEPTADO` | El comercio acepta (fija tiempo estimado) |
| `PENDIENTE` | `RECHAZADO` | El comercio rechaza |
| `ACEPTADO` | `PAGADO` | El cliente paga (`POST .../pagar`) y llega `pago.completado` |
| `ACEPTADO` | `CANCELADO` | Llega `pago.fallido` (tarjeta inválida o denegada), **o vence el plazo de pago sin que el cliente pague** |
| `PAGADO` | `ENTREGADO` | El repartidor introduce el `numeroPedido` |
| `PAGADO` | `PENDIENTE_DEVOLUCION` | El comercio anula |
| `PENDIENTE_DEVOLUCION` | `DEVUELTO` | Llega `devolucion.completada` |

Cada transición deja su fecha en el pedido (`fechaCreacion`, `fechaAceptacion`, `fechaRechazo`, `fechaPago`, `fechaEntrega`, `fechaCancelacion`, `fechaAnulacion`, `fechaDevolucion`): es la base para informes y facturación.

## Eventos

**Publica** en `delivery.exchange` (todos como `PedidoEventMessage`):

| Routing key | Cuándo |
|---|---|
| `pedido.creado` | Checkout correcto |
| `pedido.aceptado` | El comercio acepta |
| `pago.solicitado` | El cliente pulsa pagar (`POST /api/pedidos/{id}/pagar`), no al aceptar |
| `pedido.rechazado` | El comercio rechaza |
| `pedido.cancelado` | Pago denegado o plazo de pago vencido |
| `pedido.entregado` | El repartidor registra la entrega |
| `devolucion.solicitada` | El comercio anula un pedido pagado |

**Consume:**

| Cola | Routing keys | Efecto |
|---|---|---|
| `pedido.pago-resultado.queue` | `pago.completado`, `pago.fallido` | → `PAGADO` / → `CANCELADO` |
| `pedido.devolucion.queue` | `devolucion.completada` | → `DEVUELTO` |
| `pedido.comercio.queue` | `comercio.*` | Guarda si el comercio admite pedidos |

Los bindings de pago son **explícitos, no `pago.*`**: ese wildcard también capturaría `pago.solicitado`, que viaja como `PedidoEventMessage` y no deserializa en el `PagoEventMessage` del listener. El de comercio sí usa wildcard porque todas sus claves llevan `ComercioEventMessage`.

**Por qué `pedido.cancelado`.** Cuando el pago falla, fidelidad necesita el código del cupón y los puntos aplicados para deshacer la reserva, y `PagoEventMessage` no los lleva — son datos del pedido, no del cobro. Así que `pedido-service` traduce el fallo a un evento propio que sí los trae. De paso sirve igual para la cancelación por plazo vencido, que no viene de ningún pago.

Los listeners son tolerantes al desorden: si el pedido no está en el estado esperado, lo registran como warning y no persisten nada, en vez de reventar y dejar el mensaje dando vueltas.

## Dependencias

**Maven** (Spring Boot 3.3.0, Java 21): `spring-boot-starter-web`, `spring-boot-starter-data-mongodb`, `spring-boot-starter-amqp`, `delivery-contracts` 1.0.0, Lombok 1.18.30, MapStruct 1.5.5.Final, `spring-boot-starter-test`.

**Infraestructura:** MongoDB y RabbitMQ.

**Otros servicios:**

| Servicio | Cómo | Cuándo |
|---|---|---|
| `auth-service` | HTTP `POST /auth/validate` | En cada petición con Bearer |
| `comercio-service` | HTTP `GET /api/comercios/{id}/productos` | En **todo** checkout, para revalidar precios |
| `fidelidad-service` | HTTP `GET /api/cupones/validar` y `GET /api/puntos/{clienteId}/saldo?comercioId=`, con `X-Service-Key` | Al aplicar cupón y en el checkout, si hay cupón o puntos |
| `pago-service` | RabbitMQ (asíncrono) | Al aceptar y al anular |

Las dos dependencias HTTP son **síncronas y bloqueantes en el checkout**. La de comercio afecta a todos los checkouts (sin catálogo no hay precio que validar); la de fidelidad solo a los que llevan cupón o puntos.

## Configuración

| Variable | Por defecto | Qué es |
|---|---|---|
| `MONGO_USERNAME` / `MONGO_PASSWORD` | — (obligatorias) | Credenciales de Mongo |
| `MONGO_HOST` / `MONGO_PORT` | `localhost` / `27017` | Dónde está Mongo |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | Dónde está RabbitMQ |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | — (obligatorias) | Credenciales de RabbitMQ |
| `AUTH_SERVICE_URL` | `http://localhost:8081` | Base de `auth-service` |
| `COMERCIO_SERVICE_URL` | `http://localhost:8082` | Base de `comercio-service` |
| `FIDELIDAD_SERVICE_URL` | `http://localhost:8085` | Base de `fidelidad-service` |
| `SERVICE_API_KEY` | `dev-service-key-cambiar` | Clave que se envía a los endpoints internos de fidelidad. Debe coincidir con la suya |
| `PEDIDO_PAGO_TIMEOUT_MIN` | `15` | Minutos que espera un pedido `ACEPTADO` antes de cancelarse |
| `PEDIDO_PAGO_TIMEOUT_CHECK_MS` | `60000` | Cada cuánto se barren los pedidos vencidos |

## Modelo de datos (`pedido_db`)

- **`carritos`** — `clienteId` (único), `comercioId`, `items[]` (`productoId`, `nombre`, `ingredientes`, `imagenUrl`, `precio`, `cantidad`), `codigoCupon`, `puntosAplicados`
- **`pedidos`** — `numeroPedido` (único), `comercioId`, `clienteId`, `items[]` (snapshot, mismos campos que el carrito incluidos `ingredientes`/`imagenUrl`), `subtotal`, `descuentoCupon`, `descuentoPuntos`, `total`, `estado`, `tiempoEstimadoMin`, `mensajeComercio`, `motivoCancelacion`, `motivoAnulacion`, `codigoCupon`, `puntosAplicados` y las ocho fechas
- **`estados_comercio`** — `comercioId` (único), `operativo`, `fechaActualizacion`. Réplica local alimentada por los eventos `comercio.*`

## Integridad del checkout

Tres reglas que impiden que entre un pedido mal formado:

1. **El precio lo pone el catálogo, no el cliente.** El checkout pide el catálogo a `comercio-service` y **reconstruye los items** con el nombre y el precio vigentes; del carrito solo se respeta qué producto y qué cantidad. Si el precio enviado no coincide con el actual, responde **409** en vez de cobrar en silencio un importe que el cliente no vio — puede ser manipulación, pero también un catálogo que cambió mientras compraba, y en ambos casos toca refrescar. Un producto ausente del catálogo o no disponible da **400**, igual que una cantidad no positiva: sin esa comprobación, una cantidad negativa restaría del total.
2. **Un comercio suspendido no admite pedidos.** El estado se mantiene localmente desde los eventos `comercio.*`, así que no hay que preguntar a `comercio-service` en cada checkout. De un comercio del que no se sabe nada se asume que es operativo (*fail-open*): lo contrario bloquearía todos los pedidos tras un arranque en frío.
3. **Ningún pedido espera indefinidamente.** Un `ACEPTADO` que nunca recibe respuesta de la pasarela (o que el cliente nunca llega a pagar) retendría para siempre los puntos y el cupón del cliente. El barrido programado lo cancela pasado el plazo y publica `pedido.cancelado` para liberar la reserva — el plazo cuenta desde `fechaAceptacion`, así que también cubre el tiempo que tiene el cliente para elegir tarjeta y pulsar pagar, no solo el tiempo de respuesta de la pasarela.

## Detalles que conviene saber

- **El cobro lo dispara el cliente, no la aceptación del comercio.** Aceptar solo marca `ACEPTADO`; el cliente ve entonces un botón para elegir tarjeta y pulsar pagar (`POST /api/pedidos/{id}/pagar`, con `tarjetaId` de una tarjeta guardada en `pago-service`), y eso es lo único que publica `pago.solicitado`. `pago-service` valida que la tarjeta exista y sea del cliente antes de intentar el cobro simulado; si no, deniega directamente sin pasar por el sorteo aleatorio.
- **`numeroPedido`**: 6 caracteres de un alfabeto sin ambigüedades (sin `0`/`O`, sin `1`/`I`/`L`), porque el cliente se lo dicta al repartidor en la puerta. Se regenera si colisiona.
- **Los importes son un snapshot.** Al crear el pedido se congelan items y precios: cambiar el catálogo después no altera pedidos existentes.
- **El descuento por puntos nunca deja el total negativo**: se limita a lo que queda tras aplicar el cupón.
- **El barrido de timeout no tiene cerrojo distribuido.** Con varias réplicas todas lo ejecutarían; no corrompe nada (cancelar solo actúa sobre `ACEPTADO` y es idempotente), pero repite trabajo. Con más de una réplica, ShedLock o equivalente.

## Construir y ejecutar

```bash
mvn -f delivery-contracts/pom.xml install -DskipTests   # una vez
mvn -f pedido-service/pom.xml clean package
mvn -f pedido-service/pom.xml test
mvn -f pedido-service/pom.xml spring-boot:run
```

Colección Postman: `postman/pedido-service.postman_collection.json`.
