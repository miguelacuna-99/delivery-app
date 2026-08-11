# Delivery App — Backend

Gestión de pedidos a domicilio para comercios de comida. Seis microservicios Spring Boot 3.3 / Java 21, comunicados por RabbitMQ, cada uno con su base MongoDB y arquitectura hexagonal (ports & adapters).

**Multi-tenant desde el primer día**: el `comercioId` viaja en el JWT y en todos los documentos, así que la misma plataforma sirve a N comercios sin cambios de arquitectura.

## Módulos

| Módulo | Puerto | BD | Qué hace |
|---|---|---|---|
| [`delivery-contracts`](delivery-contracts/README.md) | — | — | Librería compartida: enums y mensajes de eventos, versionada con semver (`1.0.0`) |
| [`auth-service`](auth-service/README.md) | 8081 | `auth_db` | JWT, usuarios de comercio y clientes, reseteo de contraseña |
| [`comercio-service`](comercio-service/README.md) | 8082 | `comercio_db` | Ficha del comercio, suscripción a la plataforma y catálogo de productos |
| [`pedido-service`](pedido-service/README.md) | 8083 | `pedido_db` | Carrito, checkout y máquina de estados del pedido |
| [`pago-service`](pago-service/README.md) | 8084 | `pago_db` | Mock de pasarela: cobro y devolución. Solo eventos, sin REST |
| [`fidelidad-service`](fidelidad-service/README.md) | 8085 | `fidelidad_db` | Cupones y puntos |
| [`notificacion-service`](notificacion-service/README.md) | 8086 | `notificacion_db` | Bandejas del comercio: pendientes y pagados |

Infraestructura: RabbitMQ (`5672`, UI en `15672`), MongoDB (`27017`) y mongo-express en el **8090** — el 8081 lo ocupa auth-service.

## Construir

El pom agregador de la raíz construye todo en orden, empezando por los contratos:

```bash
mvn clean install              # con tests
mvn clean install -DskipTests  # sin tests
```

Cada módulo se puede construir por separado, pero antes hay que tener `delivery-contracts` instalado en el repositorio local:

```bash
mvn -f delivery-contracts/pom.xml install -DskipTests
mvn -f pedido-service/pom.xml test
```

Requisitos: JDK 21 y Maven 3.6+.

## Levantar todo

```bash
docker compose up -d --build
```

Los `Dockerfile` usan **la raíz del repo como contexto de build** (compilan primero los contratos y luego el servicio): no cambies el `context: .` del `docker-compose.yaml`.

Para ejecutar un servicio fuera de Docker contra la infraestructura dockerizada:

```
MONGO_USERNAME=root
MONGO_PASSWORD=root
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=admin
```

## Recorrido end-to-end

El orden importa: el sistema arranca vacío y hay que crear el comercio antes que su ROOT. Con las colecciones de Postman cada paso guarda automáticamente los ids y tokens que necesita el siguiente.

1. **Crear el comercio** — `POST :8082/api/comercios` con `X-Platform-Key`. Guarda el `id`.
2. **Provisionar el ROOT** — `POST :8081/api/usuarios/root` con `X-Platform-Key` y ese `comercioId`. En los logs de `auth-service` aparecerá el correo de bienvenida con la URL de reseteo.
3. **Login del ROOT** — `POST :8081/auth/login`. Devuelve `mustChangePassword: true`, que el frontal usaría para forzar el cambio.
4. **Crear el equipo** — el ROOT crea `ADMIN`, `PERSONAL` y `REPARTIDOR` en `POST :8081/api/usuarios`.
5. **Montar el catálogo** — el ADMIN crea productos en `POST :8082/api/productos`. Guarda algún `id` y su precio.
6. **Opcional: un cupón** — el ADMIN lo crea en `POST :8085/api/cupones`.
7. **Registrar un cliente y entrar** — `POST :8081/api/clientes/registro` y luego login.
8. **Carrito y checkout** — el cliente hace `PUT :8083/api/carrito` con los productos (y cupón/puntos si quiere) y luego `POST :8083/api/carrito/checkout`. Anota `id` y `numeroPedido`.
9. **El comercio acepta** — `GET :8083/api/pedidos?estado=PENDIENTE` y `POST :8083/api/pedidos/{id}/aceptar`. **Aquí se dispara el cobro**: el pedido pasa a `ACEPTADO` y, en cuanto responda la pasarela mock, a `PAGADO`.
10. **Comprobar los efectos** — bandejas en `GET :8086/api/notificaciones?tipo=...` y puntos del cliente en `GET :8085/api/puntos/me`.
11. **Entregar** — el repartidor manda el `numeroPedido` a `POST :8083/api/pedidos/entregar`.
12. **Opcional: anular y devolver** — `POST :8083/api/pedidos/{id}/anular` sobre un pedido `PAGADO` lo lleva a `PENDIENTE_DEVOLUCION` y, tras el banco mock, a `DEVUELTO`.

La pasarela mock deniega ~10 % de los cobros. Para que el recorrido salga siempre a la primera, arranca `pago-service` con `PAGO_MOCK_PROBABILIDAD_EXITO=1`.

Dos cosas que pueden sorprender por el camino, y son intencionadas:

- **El precio del carrito tiene que coincidir con el del catálogo.** El checkout revalida contra `comercio-service` y responde 409 si no cuadra. Las colecciones de Postman guardan el precio al crear el producto, así que encaja solo.
- **Si suspendes la suscripción del comercio (paso opcional), sus checkouts empiezan a fallar con 409.** Renuévala para que vuelvan a entrar pedidos.

## Documentación

- [`docs/DISENO.md`](docs/DISENO.md) — diseño completo, flujo de eventos y limitaciones conocidas
- `docs/*.puml` — arquitectura, modelo de datos, flujo del pedido, estados y reseteo de contraseña
- `README.md` de cada módulo — API, eventos, configuración y dependencias
- [`postman/`](postman/README.md) — una colección por servicio y el environment local
- `CLAUDE.md` — convenciones del repositorio para trabajar con Claude Code

## Tests

Dos niveles, ninguno necesita Docker:

- **Unitarios** — JUnit 5 + Mockito sobre los servicios de aplicación, con todos los puertos mockeados.
- **Integración del contexto web** — slices `@WebMvcTest` con MockMvc que verifican rutas, serialización JSON, los interceptores de seguridad (JWT, `X-Platform-Key`, `X-Service-Key`) y que cada excepción acabe en el código HTTP correcto.

```bash
mvn test
```

Lo que **no** cubren: Mongo, RabbitMQ y las llamadas HTTP reales entre servicios. Eso pediría Testcontainers y ataría el build a tener Docker levantado; de momento ese hueco lo tapa el recorrido end-to-end manual.

# Colecciones Postman

Una colección por microservicio más un environment con todas las URLs, claves y credenciales de la ejecución local.

## Importar

1. En Postman: **Import** → arrastra los archivos de esta carpeta (los seis `*.postman_collection.json` y el `Delivery-Local.postman_environment.json`).
2. Arriba a la derecha, selecciona el environment **Delivery Local**.

| Archivo | Servicio |
|---|---|
| `auth-service.postman_collection.json` | auth-service (8081) |
| `comercio-service.postman_collection.json` | comercio-service (8082) |
| `pedido-service.postman_collection.json` | pedido-service (8083) |
| `pago-service.postman_collection.json` | pago-service (8084), vía RabbitMQ |
| `fidelidad-service.postman_collection.json` | fidelidad-service (8085) |
| `notificacion-service.postman_collection.json` | notificacion-service (8086) |

## Cómo encajan

**No hay que copiar ids ni tokens a mano.** Cada petición guarda en el environment lo que necesitan las siguientes: los logins guardan `tokenRoot`, `tokenAdmin`, `tokenPersonal`, `tokenRepartidor` y `tokenCliente`; crear el comercio guarda `comercioId`; el checkout guarda `pedidoId` y `numeroPedido`, y así con todo.

Los usuarios y el CIF se generan con un sufijo único en cada ejecución, así que **puedes repetir el recorrido entero sin vaciar la base de datos** ni chocar con un 409 por duplicado.

## Orden para el recorrido completo

Con todo levantado (`docker compose up -d --build`):

| # | Colección | Petición |
|---|---|---|
| 1 | comercio | Crear comercio |
| 2 | auth | Provisionar ROOT del comercio |
| 3 | auth | Login ROOT |
| 4 | auth | Crear ADMIN → Login ADMIN |
| 5 | auth | Crear PERSONAL → Login PERSONAL |
| 6 | auth | Crear REPARTIDOR → Login REPARTIDOR |
| 7 | comercio | Crear producto |
| 8 | fidelidad | Crear cupón *(opcional)* |
| 9 | auth | Registro de cliente → Login CLIENTE |
| 10 | pedido | Poner productos en el carrito → Checkout |
| 11 | notificacion | Bandeja PEDIDOS PENDIENTES |
| 12 | pedido | Aceptar pedido *(dispara el cobro)* |
| 13 | notificacion | Bandeja PEDIDOS PAGADOS |
| 14 | fidelidad | Mis puntos |
| 15 | pedido | Entregar con el numeroPedido |

Entre el paso 12 y el 13 pasa algo asíncrono: al aceptar, el pedido queda `ACEPTADO` y solo llega a `PAGADO` cuando `pago-service` responde. Es cuestión de un instante, pero si consultas demasiado rápido lo verás todavía en `ACEPTADO`.

**La pasarela mock deniega ~10 % de los cobros.** Si quieres que el recorrido salga siempre a la primera, arranca `pago-service` con `PAGO_MOCK_PROBABILIDAD_EXITO=1`. Con `0` fuerzas siempre el fallo, útil para probar la rama de cancelación.

## Cosas que conviene saber

- **No existe una petición "pagar".** El cobro lo dispara el comercio al aceptar el pedido, no el cliente. Es el punto donde más se pierde quien viene de otro diseño.
- **`pago-service` no tiene API REST.** Su colección habla con la API de gestión de RabbitMQ (`:15672`, admin/admin) para publicarle eventos e inspeccionar colas. Sirve igual para diagnosticar el resto: "Ver todas las colas" enseña de un vistazo si algún servicio no está consumiendo.
- **Las peticiones que esperan un error están marcadas como tal** (`(403)`, `(400)`, `(401)` en el nombre) y su test comprueba justo ese código. No son fallos de la colección.
- **El correo es un mock.** La URL de reseteo de contraseña aparece en los logs de `auth-service` (`docker compose logs -f auth-service`); copia el token a la variable `resetToken` para usar "Fijar nueva contraseña".
- **El precio del carrito debe coincidir con el del catálogo.** El checkout lo revalida contra `comercio-service` y responde 409 si no cuadra — hay un par de peticiones en la colección de pedido que lo demuestran. Como las colecciones guardan el precio al crear el producto, el flujo normal encaja solo.
- **Los endpoints internos de fidelidad van con `X-Service-Key`**, no con JWT: los llama `pedido-service`, no un usuario.
- **Cambia `platformKey` y `serviceKey`** si arrancas los servicios con valores distintos de los de desarrollo.
