# Delivery App — Diseño de Backend

Sistema de gestión de pedidos delivery para comercios de comida a domicilio.
Multi-tenant desde el día uno: **todo documento y todo token llevan `comercioId`**, lo que permite escalar la misma plataforma a N empresas sin cambios de arquitectura. Cada web se vende a un único comercio, así que también el cliente final queda atado a uno desde su registro.

## Stack

- Java 21 + Spring Boot 3.3, Maven
- Arquitectura hexagonal (ports & adapters), mismo esqueleto que el proyecto de referencia (`domain / application / infrastructure`)
- API First: contratos OpenAPI por servicio antes de implementar
- RabbitMQ (TopicExchange `delivery.exchange`) para eventos entre servicios
- MongoDB: una base de datos por microservicio (database-per-service)
- Docker Compose para infraestructura y servicios

## Microservicios

| Servicio | Puerto | Base de datos | Responsabilidad |
|---|---|---|---|
| `auth-service` | 8081 | `auth_db` | Identidad y JWT. Usuarios de comercio (ADMIN / PERSONAL / REPARTIDOR) y Clientes (CLIENTE). Registro y modificación de datos de contacto del cliente. |
| `comercio-service` | 8082 | `comercio_db` | Datos del comercio (nombre, CIF, dirección…), **suscripción a la plataforma** (plan, estado, vencimiento) y catálogo de productos: solo ROOT/ADMIN crean productos, cambian precios, editan o eliminan. |
| `pedido-service` | 8083 | `pedido_db` | Carrito del cliente, creación del pedido, máquina de estados, aceptación/denegación por el comercio, registro de entrega por el repartidor. |
| `pago-service` (mock) | 8084 | `pago_db` | Mock de pasarela de pago con firma. Simula éxito / fallo / timeout. |
| `fidelidad-service` | 8085 | `fidelidad_db` | Cupones (X usos por usuario) y puntos (canje por % de descuento, devolución si el pago falla). |
| `notificacion-service` | 8086 | `notificacion_db` | Bandeja de notificaciones del comercio: PEDIDOS PENDIENTES y PEDIDOS PAGADOS. Consume eventos. |

## Librería de contratos versionada (`delivery-contracts`)

Módulo Maven propio (`es.delivery.manager:delivery-contracts`, versionado **semver**, actualmente `1.0.0`) que contiene los objetos compartidos entre servicios, para no replicar código:

- `contracts.model`: enums transversales — `TipoUsuario`, `EstadoPedido`, `EstadoPago`, `PlanSuscripcion`, `EstadoSuscripcion`.
- `contracts.event`: mensajes de RabbitMQ — `PedidoEventMessage`, `PagoEventMessage`, `ComercioEventMessage`, `ItemPedidoPayload` — y `RoutingKeys` (exchange y routing keys como constantes, punto único de verdad).

La `1.0.0` es la versión inicial: todo el proyecto (agregador, servicios y librería) parte de ella hasta la primera release.

Cada servicio declara la dependencia con una versión concreta (`<delivery-contracts.version>`), de modo que:

- **Añadir campos o valores nuevos** → versión *minor* (1.1.0), compatible: los servicios se actualizan cuando les toque, sin romper a los que sigan en 1.0.0.
- **Eliminar o renombrar campos** → versión *major* (2.0.0): los servicios antiguos siguen funcionando con la versión anterior hasta que migren.
- Un microservicio nuevo con objetos nuevos añade sus contratos en una versión nueva de la librería sin tocar los existentes.

Los modelos de dominio internos de cada servicio (Pedido, Cupón, Carrito…) **no** van en la librería: solo se comparte lo que cruza fronteras entre servicios (eventos y enums). Así el dominio de cada servicio evoluciona libre sin acoplar a los demás.

Cada servicio sigue la estructura hexagonal del proyecto de referencia:

```
domain/
  model/         — entidades puras
  repository/    — puertos de repositorio
  event/         — eventos y puertos de publicación
application/
  usecase/       — interfaces de caso de uso (una por operación)
  service/       — implementaciones de los casos de uso
infrastructure/
  controller/    — REST + DTOs (contrato OpenAPI)
  repository/    — adaptadores MongoDB (Document, MongoRepository, RepositoryAdapter)
  messaging/     — publishers/listeners RabbitMQ + message DTOs
  security/      — JwtAuthInterceptor, AuthServiceClient, RequestSecurityContext
  mapper/        — MapStruct
  config/        — @Configuration
```

**groupId:** `es.delivery.manager` — paquetes `es.delivery.manager.<servicio>`.

## Seguridad y multi-tenancy

1. Login en `auth-service` → JWT con claims: `userId`, `username`, `comercioId` y `tipo`.
2. Los demás servicios validan el token vía `auth-service /auth/validate` (JwtAuthInterceptor + AuthServiceClient, como en el proyecto de referencia).
3. Los claims quedan en `RequestSecurityContext` (ThreadLocal); **toda query de un usuario de comercio se filtra por su `comercioId`** — un comercio solo ve sus propias operaciones.
4. Roles:
   - `ROOT`: usuario provisionado por la plataforma al dar de alta el comercio (`mustChangePassword = true`); cambia su contraseña vía correo en el primer acceso.
   - `ADMIN`: gestiona datos del comercio, usuarios, catálogo y **cupones (crear / anular)**.
   - `PERSONAL`: acepta/deniega pedidos, ve notificaciones.
   - `REPARTIDOR`: registra el número de pedido para marcarlo ENTREGADO.
   - `CLIENTE`: se registra solo, edita sus datos, gestiona carrito y pedidos propios.

## Suscripción del comercio

La plataforma cobra a los comercios por suscripción, así que el propio comercio guarda su estado de pago frente a la plataforma (no confundir con el pago de un pedido, que es de `pago-service`).

- **Plan**: `MENSUAL` o `ANUAL`. Fija cuánto se extiende el vencimiento en cada renovación.
- **Estado**: `ACTIVA` (al día), `EN_GRACIA` (vencida pero todavía operando) y `SUSPENDIDA` (sin servicio hasta renovar). `Comercio.esOperativo()` centraliza la regla: ACTIVA y EN_GRACIA operan; SUSPENDIDA no aparece en el listado público.
- **Quién manda**: solo la plataforma, con la cabecera `X-Platform-Key` — da de alta el comercio, lo suspende por impago y lo renueva. Ni el ROOT ni el ADMIN del comercio pueden tocar su propia suscripción.
- **Renovación**: si aún no ha vencido, extiende desde la fecha de fin vigente (no se pierden los días restantes); si ya venció, cuenta desde hoy. Reactivar una suscripción suspendida emite `comercio.reactivado`.

`EN_GRACIA` está modelado y respetado por `esOperativo()`, pero todavía no hay ningún proceso que haga la transición automática ACTIVA → EN_GRACIA → SUSPENDIDA al vencer: hoy la suspensión es una acción explícita de la plataforma.

## Modelo de datos (resumen)

- **Comercio**: id, nombre, CIF, dirección, teléfono, email, activo, plan (MENSUAL/ANUAL), estadoSuscripcion (ACTIVA/EN_GRACIA/SUSPENDIDA), fechaInicioSuscripcion, fechaFinSuscripcion.
- **Producto** (catálogo, pertenece a un comercio): id, comercioId, nombre, precio, disponible.
- **Usuario** (comercio): id, comercioId, username, password (BCrypt), mail, teléfono?, tipo (ROOT/ADMIN/PERSONAL/REPARTIDOR), mustChangePassword.
- **PasswordResetToken**: userId, mail, tokenHash (la URL cifrada llega al correo), expiresAt, usado. Un solo uso; sirve para el olvido de contraseña de cualquier usuario o cliente y para el primer acceso del ROOT.
- **Cliente**: id, comercioId, username, password (BCrypt), mail, direcciónDomicilio, teléfono?, tipo (CLIENTE).
- **Carrito**: id, clienteId, comercioId, items[{productoId, nombre, precio, cantidad}], cupónAplicado?, puntosAplicados?.
- **Pedido**: id, numeroPedido (corto, solo lo conoce el cliente), comercioId, clienteId, items (snapshot **tomado del catálogo**, no del carrito), importes (subtotal, descuentoCupón, descuentoPuntos, total), estado, tiempoEstimado?, mensajeComercio?, motivoCancelación?, motivoAnulación?, timestamps.
- **EstadoComercio** (en `pedido_db`): comercioId, operativo, fechaActualización. Réplica local alimentada por `comercio.suspendido` / `comercio.reactivado`.
- **Cupón**: id, comercioId, código, descuento, usosMáximosPorUsuario, usos[{clienteId, contador}], estado (ACTIVO/ANULADO/CADUCADO), fechaCaducidad. El ADMIN del comercio crea y anula cupones; un cupón caducado o anulado no puede usarse.
- **CuentaPuntos**: clienteId, saldo, movimientos[{pedidoId, tipo GANADO/CANJEADO/DEVUELTO/RETIRADO, puntos, fecha}]. `RETIRADO` es la contrapartida de `GANADO` cuando el pedido acaba devuelto.
- **Pago** (mock): id, pedidoId, comercioId, clienteId, importe, firma, estado, fecha, fechaDevolucion.
- **Notificación**: id, comercioId, tipo (PEDIDO_PENDIENTE/PEDIDO_PAGADO), pedidoId, numeroPedido, leída, fecha.

## Estados del pedido

`PENDIENTE` → (comercio acepta, fija tiempo estimado y se dispara el cobro) `ACEPTADO` → (la pasarela autoriza) `PAGADO` → (repartidor registra nº) `ENTREGADO`

Ver también **Integridad del pedido** más abajo: precios revalidados, comercio suspendido y plazo de pago.

- `PENDIENTE` → `RECHAZADO` (mensaje opcional del comercio).
- `ACEPTADO` → `CANCELADO` si el pago falla o expira el plazo de pago → publica `pedido.cancelado` y **se devuelven puntos y uso de cupón**.
- `PAGADO` → `PENDIENTE_DEVOLUCION`: el comercio anula un pedido ya pagado; se solicita la devolución del dinero al banco (mock en pago-service).
- `PENDIENTE_DEVOLUCION` → `DEVUELTO`: el banco confirma el ingreso al cliente; fidelidad retira los puntos que ese pedido había otorgado.

**Fechas e informes**: el pedido guarda la fecha de cada transición (`fechaCreacion`, `fechaAceptacion`, `fechaRechazo`, `fechaPago`, `fechaEntrega`, `fechaCancelacion`, `fechaAnulacion`, `fechaDevolucion`). Con ellas se podrán sacar informes y generar facturación por comercio y periodo — cuando llegue esa versión, un futuro `informe-service` podrá consumir los eventos `pedido.*` / `pago.*` / `devolucion.*` sin tocar los servicios actuales.

## Flujo de eventos (RabbitMQ)

Exchange: `delivery.exchange` (topic).

Esta tabla refleja lo que hay implementado, no la intención: si se añade un consumidor, se actualiza aquí.

| Routing key | Mensaje | Publica | Consumen |
|---|---|---|---|
| `pedido.creado` | `PedidoEventMessage` | pedido-service | notificacion-service (PEDIDOS PENDIENTES), fidelidad-service (reserva puntos y uso de cupón) |
| `pedido.aceptado` | `PedidoEventMessage` | pedido-service | fidelidad-service (consolida la reserva) |
| `pedido.rechazado` | `PedidoEventMessage` | pedido-service | fidelidad-service (devuelve puntos y uso de cupón) |
| `pedido.entregado` | `PedidoEventMessage` | pedido-service | fidelidad-service (llega por el binding `pedido.*`, sin efecto) |
| `pedido.cancelado` | `PedidoEventMessage` | pedido-service (pago denegado o plazo vencido) | fidelidad-service (devuelve puntos **y uso del cupón**) |
| `pago.solicitado` | `PedidoEventMessage` | pedido-service (al aceptar) | pago-service (mock procesa el cobro) |
| `pago.completado` | `PagoEventMessage` | pago-service | pedido-service (→ PAGADO), notificacion-service (PEDIDOS PAGADOS), fidelidad-service (otorga puntos según gasto) |
| `pago.fallido` | `PagoEventMessage` | pago-service | pedido-service (→ CANCELADO, que a su vez publica `pedido.cancelado`) |
| `devolucion.solicitada` | `PedidoEventMessage` | pedido-service (comercio anula un pedido pagado) | pago-service (mock banco procesa la devolución) |
| `devolucion.completada` | `PagoEventMessage` | pago-service | pedido-service (→ DEVUELTO), fidelidad-service (retira los puntos otorgados) |
| `comercio.suspendido` | `ComercioEventMessage` | comercio-service | pedido-service (deja de admitir pedidos de ese comercio) |
| `comercio.reactivado` | `ComercioEventMessage` | comercio-service | pedido-service (vuelve a admitirlos) |

**Por qué `pedido.cancelado` y no reaccionar a `pago.fallido`.** Fidelidad necesita el código del cupón y los puntos aplicados para deshacer la reserva, y `PagoEventMessage` no los lleva: son datos del pedido, no del cobro. Encadenando `pago.fallido` → `pedido-service` → `pedido.cancelado` → `fidelidad-service`, fidelidad reacciona al **cambio de estado del pedido**, que sí trae todo lo que necesita. De regalo, ese mismo evento sirve para la cancelación por plazo vencido, que no viene de ningún pago.

Una cola por consumidor y propósito. **El binding wildcard solo es válido cuando todas las claves que captura viajan con el mismo tipo de mensaje**: `pedido.*` lo cumple, pero `pago.*` no — mezcla `pago.solicitado` (`PedidoEventMessage`) con los resultados (`PagoEventMessage`), y una cola que reciba ambos falla al deserializar. Por eso los consumidores de resultados de pago se enlazan con claves explícitas.

## Reglas de fidelidad

- **Cupones**: X usos por usuario; al agotar los usos para ese usuario, el cupón queda inutilizable para él. El ADMIN puede generar cupones nuevos y **anularlos** en cualquier momento; también se invalidan automáticamente al superar su fecha de caducidad (la caducidad se materializa de forma perezosa: al validar un cupón `ACTIVO` ya vencido se persiste como `CADUCADO`).
- **Puntos**: cada pedido pagado otorga puntos proporcionales al gasto — **1 punto por cada euro** del total cobrado. Se canjean como descuento directo a razón de **1 punto = 0,01 €**, y el descuento por puntos nunca deja el total por debajo de cero.
- **Ciclo reserva → consolidación**: al crear el pedido se descuentan los puntos aplicados y se cuenta el uso del cupón (`pedido.creado`); `pedido.aceptado` consolida esa reserva (no tiene efecto económico, la reserva ya se aplicó); `pedido.rechazado` y `pedido.cancelado` la devuelven entera, puntos **y** uso del cupón. Todo es idempotente: antes de devolver, otorgar o retirar se mira si ya hay un movimiento de ese tipo para ese pedido, de modo que un evento repetido no abona puntos dos veces. Importa porque RabbitMQ garantiza *al menos una* entrega, no exactamente una.
- **Devolución del pedido**: `devolucion.completada` retira con un movimiento `RETIRADO` los puntos que ese pedido había otorgado.

## Integridad del pedido

Tres reglas que cierran los agujeros por los que se colaba un pedido mal formado:

1. **El precio lo pone el catálogo, no el cliente.** En el checkout, `pedido-service` pide el catálogo a `comercio-service` (`ProductoPort` → `ComercioClient`) y **reconstruye los items** con el nombre y el precio vigentes. Del carrito solo se respeta *qué* se pide y *cuánta* cantidad. Si el precio enviado no coincide con el del catálogo se rechaza con **409** en vez de cobrar en silencio un importe que el cliente no vio: puede ser una manipulación, pero también un catálogo que cambió mientras compraba, y en ambos casos lo correcto es que refresque. Un producto que no está en ese catálogo o está marcado como no disponible da **400**, igual que una cantidad no positiva — sin esa comprobación, una cantidad negativa restaría del total.
2. **Un comercio suspendido no admite pedidos.** `pedido-service` consume `comercio.suspendido` / `comercio.reactivado` y mantiene una réplica local del estado (colección `estados_comercio`), así que no tiene que preguntar a `comercio-service` en cada checkout. De un comercio del que no sabe nada asume que es operativo (*fail-open*): lo contrario dejaría el sistema sin poder pedir a nadie tras un arranque en frío.
3. **Un pedido no se queda esperando para siempre.** Un pedido `ACEPTADO` espera a que responda la pasarela; si el mensaje se pierde se quedaría bloqueado reteniendo los puntos y el cupón del cliente. Un barrido programado (`pedido.pago.timeout-min`, 15 min por defecto) lo pasa a `CANCELADO` y libera la reserva.

## Limitaciones conocidas

1. **`pago-service` no expone API REST.** Es puramente dirigido por eventos; para probarlo se publica en el exchange (ver su colección de Postman). Es una decisión, no una carencia.
2. **El barrido de timeout no tiene cerrojo distribuido.** Con varias réplicas de `pedido-service` todas ejecutarían el barrido. No corrompe nada — cancelar solo actúa sobre pedidos `ACEPTADO` y es idempotente — pero repite trabajo. Con más de una réplica conviene ShedLock o equivalente.
3. **`EN_GRACIA` no se activa sola.** Está modelado y respetado por `esOperativo()`, pero no hay proceso que degrade `ACTIVA → EN_GRACIA → SUSPENDIDA` al vencer la suscripción: hoy suspender es una acción explícita de la plataforma.
4. **Los tests de integración no levantan infraestructura.** Cubren el contexto web de cada servicio (rutas, JSON, interceptores de seguridad y traducción de excepciones a códigos HTTP) con los puertos mockeados. Ni Mongo ni RabbitMQ ni las llamadas HTTP reales entre servicios se ejercitan: eso pediría Testcontainers, que ataría el build a tener Docker en marcha.
5. **La conversión de puntos es global.** 1 punto = 0,01 € y 1 punto por euro gastado están como constantes en el código; si se quiere ajustar por comercio, hay que externalizarlas.

## Documentación por módulo

Cada microservicio y la librería tienen un `README.md` propio con su API, sus eventos, su configuración y sus dependencias. Este documento es la vista de conjunto; el README de cada módulo es el detalle.

En `postman/` hay una colección por servicio y un environment `Delivery Local`, con captura automática de tokens e ids entre peticiones.

## Diagramas

- `arquitectura-microservicios.puml` — componentes y comunicaciones
- `modelo-datos.puml` — diagrama de clases / colecciones
- `flujo-pedido.puml` — secuencia completa del pedido
- `estados-pedido.puml` — máquina de estados del pedido
- `flujo-password-reset.puml` — alta del ROOT del comercio y reseteo de contraseña por correo
