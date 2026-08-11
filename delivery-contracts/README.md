# delivery-contracts

Librería compartida con **todo lo que cruza la frontera entre microservicios**: los enums que varios servicios entienden y los mensajes que viajan por RabbitMQ. No es un servicio: no tiene puerto, ni base de datos, ni Spring.

La regla que la justifica: si dos servicios tienen que ponerse de acuerdo sobre la forma de un dato, ese dato vive aquí. Si un modelo solo lo usa un servicio (`Pedido`, `Cupon`, `Carrito`…), **no** entra — cada dominio evoluciona libre.

- **Coordenadas Maven:** `es.delivery.manager:delivery-contracts:1.0.0`
- **Paquete raíz:** `es.delivery.manager.contracts`

## Contenido

### `contracts.model` — enums transversales

| Enum | Valores | Quién lo usa |
|---|---|---|
| `TipoUsuario` | `ROOT`, `ADMIN`, `PERSONAL`, `REPARTIDOR`, `CLIENTE` | Todos (va en el JWT) |
| `EstadoPedido` | `PENDIENTE`, `ACEPTADO`, `RECHAZADO`, `PAGADO`, `ENTREGADO`, `CANCELADO`, `PENDIENTE_DEVOLUCION`, `DEVUELTO` | pedido, y quien lea eventos de pedido |
| `EstadoPago` | `PENDIENTE`, `COMPLETADO`, `FALLIDO`, `DEVUELTO` | pago, pedido |
| `PlanSuscripcion` | `MENSUAL`, `ANUAL` | comercio |
| `EstadoSuscripcion` | `ACTIVA`, `EN_GRACIA`, `SUSPENDIDA` | comercio |

### `contracts.event` — mensajes y routing keys

| Clase | Viaja en | Campos clave |
|---|---|---|
| `PedidoEventMessage` | `pedido.*` (incluido `pedido.cancelado`), `pago.solicitado`, `devolucion.solicitada` | `routingKey`, `pedidoId`, `numeroPedido`, `comercioId`, `clienteId`, `items`, importes, `estado`, `codigoCupon`, `puntosAplicados`, `timestamp` |
| `PagoEventMessage` | `pago.completado`, `pago.fallido`, `devolucion.completada` | `routingKey`, `pagoId`, `pedidoId`, `comercioId`, `clienteId`, `importe`, `estado`, `timestamp` |
| `ComercioEventMessage` | `comercio.suspendido`, `comercio.reactivado` | `routingKey`, `comercioId`, `nombre`, `estadoSuscripcion`, `fechaFinSuscripcion`, `timestamp` |
| `ItemPedidoPayload` | dentro de `PedidoEventMessage` | `productoId`, `nombre`, `precio`, `cantidad` |

`RoutingKeys` centraliza el nombre del exchange (`delivery.exchange`) y todas las routing keys como constantes. **Nunca escribas la routing key como literal**: un typo en un `@RabbitListener` no falla al compilar, falla en producción con una cola que no recibe nada.

> ⚠️ **Cuidado con los wildcards.** `RoutingKeys` ofrece `WILDCARD_PEDIDO` (`pedido.*`), `WILDCARD_PAGO` (`pago.*`), `WILDCARD_DEVOLUCION` y `WILDCARD_COMERCIO`, pero un binding wildcard solo es correcto si **todas** las claves que captura llevan el mismo tipo de mensaje. `pedido.*` lo cumple. `pago.*` **no**: mezcla `pago.solicitado` (que es un `PedidoEventMessage`) con los resultados de pago (`PagoEventMessage`), y una cola que reciba ambos revienta al deserializar. Para resultados de pago, enlaza `PAGO_COMPLETADO` y `PAGO_FALLIDO` explícitamente.

## Dependencias

Solo **Lombok 1.18.30** (`provided`, para los `@Data`/`@Builder`). Ni Spring, ni Jackson, ni AMQP: los mensajes son POJOs con constructor vacío para que cualquier serializador los pueda reconstruir.

Se compila con Java 21 y no hereda de `spring-boot-starter-parent`, así que el `maven-compiler-plugin` se configura a mano en su `pom.xml`.

## Política de versionado (semver)

| Cambio | Versión | Efecto |
|---|---|---|
| Añadir un campo, un enum nuevo o un valor de enum | **minor** (1.0.0 → 1.1.0) | Compatible: los servicios que sigan en la anterior no se enteran |
| Eliminar o renombrar un campo, quitar un valor de enum | **major** (1.0.0 → 2.0.0) | Rompe: cada servicio migra cuando puede, mientras tanto se queda en la anterior |

Cada servicio fija la versión que consume en la property `<delivery-contracts.version>` de su `pom.xml`. Hoy los seis van a `1.0.0`, la versión inicial.

## Construir

```bash
mvn -f delivery-contracts/pom.xml clean install
```

Hay que instalarlo en el repositorio local **antes** que cualquier servicio. El pom agregador de la raíz ya lo construye primero, y los `Dockerfile` de cada servicio lo compilan desde el contexto de la raíz del repo — por eso el `context: .` del `docker-compose.yaml` no se toca.
