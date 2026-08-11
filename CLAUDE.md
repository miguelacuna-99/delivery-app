# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Backend de gestion de pedidos delivery para comercios de comida a domicilio. Spring Boot 3.3 / Java 21, seis microservicios independientes comunicados por RabbitMQ; todos con Arquitectura Hexagonal (ports & adapters). Multi-tenant: `comercioId` viaja en el JWT y en todos los documentos.

El diseno completo y los diagramas PlantUML estan en `docs/` (`DISENO.md`, `arquitectura-microservicios.puml`, `modelo-datos.puml`, `flujo-pedido.puml`, `estados-pedido.puml`).

Cada modulo tiene su propio `README.md` con su API, sus eventos y sus dependencias — es la referencia de detalle por servicio, y la que hay que actualizar al tocar endpoints o eventos. En `postman/` hay una coleccion por servicio mas el environment local.

## groupId and package naming

**groupId:** `es.delivery.manager`

| Service | Package root | Puerto | DB |
|---|---|---|---|
| `delivery-contracts` (libreria compartida, sin puerto/DB) | `es.delivery.manager.contracts` | — | — |
| `auth-service` | `es.delivery.manager.auth` | 8081 | `auth_db` |
| `comercio-service` | `es.delivery.manager.comercio` | 8082 | `comercio_db` |
| `pedido-service` | `es.delivery.manager.pedido` | 8083 | `pedido_db` |
| `pago-service` (mock pasarela) | `es.delivery.manager.pago` | 8084 | `pago_db` |
| `fidelidad-service` | `es.delivery.manager.fidelidad` | 8085 | `fidelidad_db` |
| `notificacion-service` | `es.delivery.manager.notificacion` | 8086 | `notificacion_db` |

mongo-express queda en el puerto host **8090** (el 8081 lo usa auth-service).

## Build and Run

Desde la raiz, el pom agregador construye todo en orden (contratos primero):

```bash
mvn clean install -DskipTests
```

Cada servicio tambien se construye por separado desde su directorio, pero requiere haber instalado antes `delivery-contracts` en el repositorio local (`mvn -f delivery-contracts/pom.xml install`):

```bash
mvn clean package -DskipTests   # build
mvn test                        # tests
mvn spring-boot:run             # arrancar en local
```

Todo dockerizado (infra + servicios):

```bash
docker compose up -d --build
```

Variables de entorno para ejecucion local (fuera de Docker):

```
MONGO_USERNAME=root
MONGO_PASSWORD=root
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=admin
```

## Architecture

### Hexagonal layers (same structure in every service)

```
domain/
  model/         — entidades puras
  repository/    — puertos de repositorio
  event/         — eventos y puertos de publicacion
  service/       — puertos de dominio (JwtPort, PasswordPort en auth)
application/
  usecase/       — interfaces de caso de uso (una por operacion)
  service/       — implementaciones de los casos de uso
infrastructure/
  controller/    — REST controllers + DTOs
  repository/    — adaptadores MongoDB (Document, MongoRepository, RepositoryAdapter)
  messaging/     — publishers/listeners RabbitMQ + message DTOs
  security/      — JwtAuthInterceptor, AuthServiceClient, RequestSecurityContext
  mapper/        — MapStruct (domain <-> DTO <-> Document)
  config/        — Spring @Configuration
```

### Event flow

Exchange unico: `delivery.exchange` (TopicExchange).

- `pedido-service` publica: `pedido.creado`, `pedido.aceptado`, `pedido.rechazado`, `pedido.cancelado`, `pago.solicitado`, `pedido.entregado`
- `pago-service` consume `pago.solicitado` y publica: `pago.completado`, `pago.fallido`
- `pedido-service` consume `pago.completado` (→ PAGADO) y `pago.fallido` (→ CANCELADO, que publica `pedido.cancelado`)
- `fidelidad-service` consume `pedido.*` y `pago.completado` (consolidar/devolver puntos y cupones, otorgar puntos por gasto)
- `notificacion-service` consume `pedido.creado` (PEDIDOS PENDIENTES) y `pago.completado` (PEDIDOS PAGADOS)
- `comercio-service` publica `comercio.suspendido` / `comercio.reactivado`; los consume `pedido-service` para dejar de admitir pedidos de ese comercio

**`pedido.cancelado` existe porque fidelidad necesita el cupon y los puntos del pedido para deshacer la reserva, y `PagoEventMessage` no los lleva.** Por eso fidelidad no escucha `pago.fallido`: escucha el cambio de estado del pedido, que si trae esos datos — y sirve igual para la cancelacion por plazo de pago vencido.
- Devoluciones: `pedido-service` publica `devolucion.solicitada` (comercio anula pedido pagado) → `pago-service` simula la devolucion del banco y publica `devolucion.completada` → `pedido-service` (→ DEVUELTO) y `fidelidad-service` (retira puntos)

**Un binding wildcard solo vale si todas las routing keys que captura viajan con el mismo tipo de mensaje.** `pago.*` mezcla `pago.solicitado` (`PedidoEventMessage`) con `pago.completado`/`pago.fallido` (`PagoEventMessage`), asi que los consumidores de resultados de pago usan bindings explicitos. `pedido.*` si es seguro: todas sus claves son `PedidoEventMessage`.

### Auth flow

1. Login en `auth-service` → JWT con claims `userId`, `username`, `comercioId` (null para clientes), `tipo` (ROOT / ADMIN / PERSONAL / REPARTIDOR / CLIENTE)
2. Los demas servicios validan el token via HTTP `auth-service /auth/validate` (JwtAuthInterceptor + AuthServiceClient)
3. Claims en `RequestSecurityContext` (ThreadLocal); toda query de usuario de comercio se filtra por su `comercioId`
4. El usuario `ROOT` de cada comercio lo provisiona la plataforma (`mustChangePassword = true`); cambia la contrasena via correo
5. Olvido de contrasena (usuarios y clientes): `PasswordResetToken` de un solo uso con caducidad (se guarda solo el hash SHA-256), enviado como URL al correo a traves de `EmailPort` (adaptador mock `LoggingEmailAdapter` que lo escribe en el log)

Endpoints de auth-service (implementados):

| Endpoint | Proteccion | Descripcion |
|---|---|---|
| `POST /auth/login` | publico | Login de usuarios de comercio y clientes; devuelve token, tipo y mustChangePassword |
| `POST /auth/validate` | publico (lo llaman los demas servicios) | 200 valid=true + claims, o valid=false |
| `POST /auth/password/forgot` | publico | Siempre 204 (no revela si el mail existe) |
| `POST /auth/password/reset` | publico | Token de un solo uso; 400 si usado/caducado |
| `POST /api/clientes/registro` | publico | Alta de cliente |
| `PUT /api/clientes/me/contacto` | Bearer CLIENTE | El cliente modifica sus datos de contacto |
| `POST /api/usuarios/root` | header `X-Platform-Key` (`platform.api-key`) | La plataforma provisiona el ROOT de un comercio; dispara correo de bienvenida con URL de reseteo |
| `POST /api/usuarios` | Bearer ROOT o ADMIN | ROOT crea cualquier tipo de usuario de comercio (ROOT/ADMIN/PERSONAL/REPARTIDOR); ADMIN solo PERSONAL/REPARTIDOR; CLIENTE nunca por aqui. El comercioId siempre sale del token |

### Estados del pedido

`PENDIENTE` → `ACEPTADO` (comercio acepta, fija tiempo estimado) → `PAGADO` → `ENTREGADO` (repartidor registra `numeroPedido`).
`PENDIENTE` → `RECHAZADO` (mensaje opcional). `ACEPTADO` → `CANCELADO` si el pago falla o expira → fidelidad devuelve puntos y uso de cupon.
`PAGADO` → `PENDIENTE_DEVOLUCION` (comercio anula pedido pagado; `devolucion.solicitada` al banco mock) → `DEVUELTO` (`devolucion.completada`; fidelidad retira los puntos otorgados).

Todas las transiciones quedan fechadas en el Pedido (`fechaCreacion`, `fechaAceptacion`, `fechaRechazo`, `fechaPago`, `fechaEntrega`, `fechaCancelacion`, `fechaAnulacion`, `fechaDevolucion`) — base para informes y facturacion.

### Shared contracts (`delivery-contracts`)

Libreria Maven versionada con **semver** (actualmente `1.0.0`) con lo que cruza fronteras entre servicios: enums transversales (`TipoUsuario`, `EstadoPedido`, `EstadoPago`, `PlanSuscripcion`, `EstadoSuscripcion` en `contracts.model`), mensajes de eventos (`PedidoEventMessage`, `PagoEventMessage`, `ComercioEventMessage`, `ItemPedidoPayload`) y `RoutingKeys` (constantes de exchange/routing keys — nunca usar literales). Cada servicio fija su version via la property `<delivery-contracts.version>`.

Politica de evolucion: anadir campos/valores = minor (compatible); eliminar/renombrar = major (los servicios antiguos siguen con la version anterior hasta migrar). Los modelos de dominio internos de cada servicio NO van en la libreria — solo contratos compartidos.

Los Dockerfiles usan la **raiz del repo como contexto de build** (compilan `delivery-contracts` y luego el servicio); no cambiar el `context: .` del docker-compose.

### Key design decisions

- `auth-service` usa `spring-security-crypto` solo para BCrypt — sin filter chain de Spring Security; es el unico servicio sin AMQP
- `pago-service` es un **mock** de pasarela de firma: consume `pago.solicitado`, simula exito/fallo y publica el resultado; tambien simula la devolucion bancaria (`devolucion.solicitada` → `devolucion.completada`, Pago → DEVUELTO)
- Catalogo de productos: solo ROOT/ADMIN del comercio crean, editan precios o eliminan productos
- **El precio que se cobra sale del catalogo, nunca del carrito.** En el checkout `pedido-service` pide el catalogo a `comercio-service` (`ProductoPort`) y reconstruye los items; del carrito solo se respeta que producto y que cantidad. Precio distinto = 409; producto ausente, no disponible o cantidad no positiva = 400
- Comunicacion entre servicios: los endpoints internos de `fidelidad-service` (validar cupon, saldo) no llevan JWT sino la cabecera `X-Service-Key` (`service.api-key`), porque los llama `pedido-service`, no un usuario
- `numeroPedido` es un codigo corto que solo recibe el cliente; el repartidor lo introduce para marcar ENTREGADO
- Suscripcion del comercio: `PlanSuscripcion` (MENSUAL/ANUAL) + `EstadoSuscripcion` (ACTIVA/EN_GRACIA/SUSPENDIDA). `Comercio.esOperativo()` centraliza la regla — ACTIVA y EN_GRACIA operan, SUSPENDIDA no. Solo la plataforma (`X-Platform-Key`) da de alta comercios, suspende y renueva
- Fidelidad funciona como reserva → consolidacion: reserva al crear pedido, consolida en `pedido.aceptado`, devuelve en `pago.fallido` / `pedido.rechazado`
- Conversion de fidelidad: 1 punto = 0,01 EUR de descuento al canjear; se gana 1 punto por cada EUR pagado (`pago.completado`)
- Cupones: estado `ACTIVO / ANULADO / CADUCADO` + `fechaCaducidad`; solo el ADMIN del comercio crea o anula cupones (`Cupon.esUsable(ahora)` centraliza la validacion)
- MapStruct requiere el orden de annotation processors: Lombok → MapStruct → lombok-mapstruct-binding (ya configurado en todos los poms)

## Testing

Dos niveles, ambos sin infraestructura externa — `mvn test` no necesita Docker:

- **Unitarios** (la mayoria): JUnit 5 + Mockito, se mockean todos los puertos y se inyecta el servicio con `@InjectMocks`.
- **Integracion del contexto web** (`*ControllerIntegrationTest`, `*IntegrationTest`): slices `@WebMvcTest` con MockMvc que cubren rutas, serializacion JSON, interceptores de seguridad y la traduccion de excepciones a codigos HTTP. Los casos de uso van con `@MockBean` y el mapper generado por MapStruct con `@Import(XMapperImpl.class)`.

No hay tests contra Mongo ni RabbitMQ reales: eso pediria Testcontainers, que ataria el build a tener Docker en marcha. Nombrar una clase `*IT` haria que Surefire **no** la ejecute — usar el sufijo `IntegrationTest`.
