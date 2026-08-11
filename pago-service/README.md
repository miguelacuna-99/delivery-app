# pago-service

Mock de la pasarela de pago. Simula el cobro con firma y la devolución del banco, para poder recorrer el flujo completo del pedido sin integrar una pasarela real.

**No expone API REST.** Es puramente dirigido por eventos: escucha en RabbitMQ y responde en RabbitMQ. Para probarlo se publica un mensaje en el exchange (la colección de Postman lo hace por ti).

- **Puerto:** 8084 (solo lo abre Spring Boot; no hay endpoints) · **Base de datos:** `pago_db` · **Paquete:** `es.delivery.manager.pago`

## Eventos

**Consume:**

| Cola | Routing key | Mensaje | Efecto |
|---|---|---|---|
| `pago.solicitado.queue` | `pago.solicitado` | `PedidoEventMessage` | Simula el cobro del `total` y publica el resultado |
| `pago.devolucion-solicitada.queue` | `devolucion.solicitada` | `PedidoEventMessage` | Simula el ingreso al cliente y publica el resultado |

**Publica** (todos como `PagoEventMessage`):

| Routing key | Cuándo | Quién escucha |
|---|---|---|
| `pago.completado` | La pasarela autoriza | pedido (→ PAGADO), notificacion (bandeja PAGADOS), fidelidad (otorga puntos) |
| `pago.fallido` | La pasarela deniega | pedido (→ CANCELADO), fidelidad (devuelve puntos) |
| `devolucion.completada` | El "banco" confirma la devolución | pedido (→ DEVUELTO), fidelidad (retira puntos) |

## Cómo funciona el mock

`MockPasarelaAdapter` implementa el puerto `PasarelaPort`:

- **Autoriza o deniega al azar**, con probabilidad configurable (`pago.mock.probabilidad-exito`, por defecto `0.9`). Para forzar todos los pagos correctos en una prueba end-to-end, ponlo a `1`; a `0` para probar la rama de fallo.
- **Genera una firma** SHA-256 de `pedidoId | importe | timestamp`, imitando el resguardo que devolvería una pasarela real.

Sustituirlo por una pasarela de verdad es implementar `PasarelaPort` en otro adaptador: ni el caso de uso ni el dominio cambian.

## Reglas de negocio

- **El cobro es idempotente por pedido.** Si llega un `pago.solicitado` repetido y ya existe un pago para ese `pedidoId`, se registra un warning y no se cobra otra vez ni se republica el resultado. El índice único sobre `pedidoId` lo refuerza en la base de datos.
- **Solo se devuelve lo que se cobró.** Una `devolucion.solicitada` sobre un pago que no está `COMPLETADO` (o que no existe) se ignora con un warning: no se inventa un `devolucion.completada` que dejaría el pedido en `DEVUELTO` sin haber devuelto nada.

## Dependencias

**Maven** (Spring Boot 3.3.0, Java 21): `spring-boot-starter-web`, `spring-boot-starter-data-mongodb`, `spring-boot-starter-amqp`, `delivery-contracts` 1.0.0, Lombok 1.18.30, MapStruct 1.5.5.Final, `spring-boot-starter-test`.

`spring-boot-starter-web` está aunque no haya controladores: mantiene el servicio homogéneo con el resto y deja la puerta abierta a endpoints de consulta.

**Infraestructura:** MongoDB y RabbitMQ.

**Otros servicios:** ninguno de forma directa. **No valida tokens** y por eso no depende de `auth-service` — no tiene nada que autenticar. En el `docker-compose.yaml` es el único servicio que no espera a `auth-service`.

## Configuración

| Variable | Por defecto | Qué es |
|---|---|---|
| `MONGO_USERNAME` / `MONGO_PASSWORD` | — (obligatorias) | Credenciales de Mongo |
| `MONGO_HOST` / `MONGO_PORT` | `localhost` / `27017` | Dónde está Mongo |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | Dónde está RabbitMQ |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | — (obligatorias) | Credenciales de RabbitMQ |
| `PAGO_MOCK_PROBABILIDAD_EXITO` | `0.9` | Probabilidad [0..1] de que la pasarela autorice |

## Modelo de datos (`pago_db`)

- **`pagos`** — `pedidoId` (único), `comercioId`, `clienteId`, `importe`, `firma`, `estado` (`PENDIENTE`/`COMPLETADO`/`FALLIDO`/`DEVUELTO`), `fecha`, `fechaDevolucion`

## Cómo probarlo sin el resto del sistema

Publica directamente en el exchange desde la UI de RabbitMQ (`http://localhost:15672`, admin/admin) o con la colección `postman/pago-service.postman_collection.json`, que usa la API de gestión de RabbitMQ.

Publica en `delivery.exchange` con routing key `pago.solicitado`, cabecera `__TypeId__ = es.delivery.manager.contracts.event.PedidoEventMessage` y este cuerpo:

```json
{
  "routingKey": "pago.solicitado",
  "pedidoId": "pedido-de-prueba",
  "comercioId": "comercio-1",
  "clienteId": "cliente-1",
  "total": 18.80,
  "estado": "ACEPTADO"
}
```

La cabecera `__TypeId__` importa: es lo que el conversor de Jackson usa para saber qué está recibiendo.

## Construir y ejecutar

```bash
mvn -f delivery-contracts/pom.xml install -DskipTests   # una vez
mvn -f pago-service/pom.xml clean package
mvn -f pago-service/pom.xml test
mvn -f pago-service/pom.xml spring-boot:run
```
