# pago-service

Mock de la pasarela de pago. Simula el cobro con firma y la devolución del banco, para poder recorrer el flujo completo del pedido sin integrar una pasarela real. También guarda las tarjetas del cliente.

**El cobro en sí sigue siendo 100% dirigido por eventos**: escucha en RabbitMQ y responde en RabbitMQ. Lo único síncrono es la gestión de tarjetas guardadas (`/api/tarjetas`), que necesita el cliente para elegir con qué pagar.

- **Puerto:** 8084 · **Base de datos:** `pago_db` · **Paquete:** `es.delivery.manager.pago`

## API REST

| Método | Ruta | Protección | Qué hace |
|---|---|---|---|
| `POST` | `/api/tarjetas` | Bearer `CLIENTE` | Guarda una tarjeta para el cliente del token. Solo se persisten marca, últimos 4 dígitos y caducidad — nunca el número completo ni el CVV |
| `GET` | `/api/tarjetas` | Bearer `CLIENTE` | Mis tarjetas guardadas |
| `DELETE` | `/api/tarjetas/{id}` | Bearer `CLIENTE` | Elimina una tarjeta mía (404 si es de otro cliente, no confirma que exista) |

Errores: 400 número de tarjeta inválido (falla el algoritmo de Luhn), sin titular o ya caducada · 404 tarjeta inexistente o de otro cliente · 403 tipo de usuario distinto de `CLIENTE` · 401 token inválido.

## Eventos

**Consume:**

| Cola | Routing key | Mensaje | Efecto |
|---|---|---|---|
| `pago.solicitado.queue` | `pago.solicitado` | `PedidoEventMessage` | Valida la tarjeta (`tarjetaId` del mensaje), simula el cobro del `total` y publica el resultado |
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

Antes de tirar de la pasarela, `PagoService` comprueba que el `tarjetaId` del mensaje exista y sea del mismo `clienteId`: si no, deniega directamente (sin firma, sin pasar por el sorteo) — así un cliente no puede "pagar" con el id de la tarjeta de otro.

Sustituir el mock por una pasarela de verdad es implementar `PasarelaPort` en otro adaptador: ni el caso de uso ni el dominio cambian.

## Reglas de negocio

- **El cobro es idempotente por pedido.** Si llega un `pago.solicitado` repetido y ya existe un pago para ese `pedidoId`, se registra un warning y no se cobra otra vez ni se republica el resultado. El índice único sobre `pedidoId` lo refuerza en la base de datos.
- **Solo se devuelve lo que se cobró.** Una `devolucion.solicitada` sobre un pago que no está `COMPLETADO` (o que no existe) se ignora con un warning: no se inventa un `devolucion.completada` que dejaría el pedido en `DEVUELTO` sin haber devuelto nada.
- **De la tarjeta solo se guarda lo imprescindible.** Nunca el número completo ni el CVV (que ni siquiera viaja hasta el backend): solo marca, últimos 4 dígitos, titular y caducidad. El número se valida con el algoritmo de Luhn al guardarla.

## Dependencias

**Maven** (Spring Boot 3.3.0, Java 21): `spring-boot-starter-web`, `spring-boot-starter-data-mongodb`, `spring-boot-starter-amqp`, `delivery-contracts` 1.0.0, Lombok 1.18.30, MapStruct 1.5.5.Final, `spring-boot-starter-test`.

**Infraestructura:** MongoDB y RabbitMQ.

**Otros servicios:** `auth-service` — `/api/tarjetas` valida el Bearer contra `POST /auth/validate`, igual que el resto de servicios con endpoints de usuario. El cobro en sí no depende de nada síncrono: sigue resolviéndose solo con lo que trae el propio evento `pago.solicitado`.

## Configuración

| Variable | Por defecto | Qué es |
|---|---|---|
| `MONGO_USERNAME` / `MONGO_PASSWORD` | — (obligatorias) | Credenciales de Mongo |
| `MONGO_HOST` / `MONGO_PORT` | `localhost` / `27017` | Dónde está Mongo |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | Dónde está RabbitMQ |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | — (obligatorias) | Credenciales de RabbitMQ |
| `PAGO_MOCK_PROBABILIDAD_EXITO` | `0.9` | Probabilidad [0..1] de que la pasarela autorice |
| `AUTH_SERVICE_URL` | `http://localhost:8081` | Base de `auth-service`, para validar el Bearer de `/api/tarjetas` |

## Modelo de datos (`pago_db`)

- **`pagos`** — `pedidoId` (único), `comercioId`, `clienteId`, `tarjetaId`, `importe`, `firma`, `estado` (`PENDIENTE`/`COMPLETADO`/`FALLIDO`/`DEVUELTO`), `fecha`, `fechaDevolucion`
- **`tarjetas`** — `clienteId` (indexado), `titular`, `marca` (`VISA`/`MASTERCARD`/`OTRA`), `ultimos4`, `mesExpiracion`, `anioExpiracion`, `fechaAlta`

## Cómo probarlo sin el resto del sistema

Publica directamente en el exchange desde la UI de RabbitMQ (`http://localhost:15672`, admin/admin) o con la colección `postman/pago-service.postman_collection.json`, que usa la API de gestión de RabbitMQ.

Publica en `delivery.exchange` con routing key `pago.solicitado`, cabecera `__TypeId__ = es.delivery.manager.contracts.event.PedidoEventMessage` y este cuerpo:

```json
{
  "routingKey": "pago.solicitado",
  "pedidoId": "pedido-de-prueba",
  "comercioId": "comercio-1",
  "clienteId": "cliente-1",
  "tarjetaId": "id-de-una-tarjeta-guardada-de-ese-cliente",
  "total": 18.80,
  "estado": "ACEPTADO"
}
```

Si `tarjetaId` no existe o no pertenece a ese `clienteId`, el pago se deniega directamente (sin pasar por el sorteo aleatorio) — hace falta haber guardado antes una tarjeta real con `POST /api/tarjetas` para probar la rama de éxito.

La cabecera `__TypeId__` importa: es lo que el conversor de Jackson usa para saber qué está recibiendo.

## Construir y ejecutar

```bash
mvn -f delivery-contracts/pom.xml install -DskipTests   # una vez
mvn -f pago-service/pom.xml clean package
mvn -f pago-service/pom.xml test
mvn -f pago-service/pom.xml spring-boot:run
```
