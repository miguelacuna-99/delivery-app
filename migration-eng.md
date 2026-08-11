# Migración del código a inglés — plan de ejecución

Plan para renombrar toda la aplicación (paquetes, clases, métodos, variables, mensajes y textos) de español a inglés, minimizando riesgo y consumo de tokens. **Este documento es el plan; la ejecución se hará en otra sesión.**

## Alcance medido (30/07/2026)

- 317 ficheros Java (~10.500 líneas), 14 de configuración (yml/properties/xml), 11 markdown, `docs/` (DISENO.md + 4 `.puml`) y colecciones Postman.
- ~5.900 apariciones solo de los 12 sustantivos de dominio principales; con verbos, textos y docs, **10.000–15.000 apariciones** en total.
- La mayoría de los 317 ficheros y los paquetes cambian de nombre (`PedidoService` → `OrderService`, `es.delivery.manager.pedido` → `es.delivery.manager.order`).

**Presupuesto estimado: 300–400k tokens** si se sigue la estrategia scriptada de abajo (frente a 1,5–2,5M haciéndolo fichero a fichero).

## Las tres capas — no cuestan lo mismo

| Capa | Qué incluye | Riesgo | ¿Entra en este plan? |
|---|---|---|---|
| **1. Código interno** | Clases, métodos, variables, paquetes, comentarios, nombres de test | Solo rompe compilación hasta terminar | ✅ Sí |
| **2. Contratos externos** | Routing keys (`pedido.creado`…), campos JSON de la API, claims del JWT (`comercioId`, `tipo`), valores de enums que viajan (`TipoUsuario`, `EstadoPedido`…), nombres de BD/colecciones Mongo, `delivery-contracts` | **Cambio major** (semver): invalida Postman, datos ya guardados en Mongo y mensajes en vuelo | ⚠️ Fase opcional, decidir aparte |
| **3. Textos de usuario** | Mensajes de error, emails, logs | Trivial | ✅ Sí |

Regla de oro: **la capa 1 y la 3 se pueden hacer sin tocar la 2**. Un `OrderService` puede seguir publicando la routing key `pedido.creado` y serializando el campo JSON `comercioId`. Si se toca la capa 2, es `delivery-contracts` **2.0.0** y migración de datos.

## Fase 0 — Preparación (imprescindible, ~5k tokens)

1. **El proyecto NO es un repositorio git.** Antes de tocar nada: `git init && git add -A && git commit -m "baseline pre-migración"`. Sin esto no hay vuelta atrás barata.
2. Verificar línea base verde: `mvn clean install` (con tests) debe pasar antes de empezar.
3. Crear el fichero de mapeo `migration-glossary.txt` a partir del glosario de abajo (formato `español=inglés`, una línea por término). El script lo consume; Claude no debe teclear los reemplazos uno a uno.

## Estrategia scriptada (la que ahorra tokens)

El trabajo mecánico lo hace un script (PowerShell o bash), no el modelo. Claude solo: (a) genera el script una vez, (b) lo ejecuta por servicio, (c) compila y **arregla únicamente los errores de compilación**, (d) verifica con `Grep`, nunca leyendo ficheros enteros.

Reglas del script de reemplazo:

- **Términos más largos primero** (`devolucion` antes que `devol`, `pagado` antes que `pago`, `cancelacion` antes que `cancelado`... ordenar el glosario por longitud descendente evita corrupciones tipo `paymentdo`).
- Generar automáticamente las 3 variantes de cada término: `pedido→order`, `Pedido→Order`, `PEDIDO→ORDER`. Cubrir plurales explícitamente en el glosario (`pedidos=orders`), no con heurísticas.
- Reemplazar **solo dentro de límites de palabra/camelCase** cuando sea posible; revisar con grep los términos cortos propensos a colisión.
- Excluir `target/`, `.git/` y — hasta la fase que les toque — `postman/`, `docs/` y los `RoutingKeys`/DTOs de mensajes si la capa 2 se pospone.
- Renombrado de ficheros y directorios (paquetes) con `git mv` scriptado a partir del mismo glosario.

## Fase 1 — Código interno, un servicio por tanda (~40–50k tokens/servicio)

Orden: `delivery-contracts` → `auth-service` → `comercio-service` → `pedido-service` → `pago-service` → `fidelidad-service` → `notificacion-service` (el orden del pom agregador).

Por cada servicio:

1. Ejecutar el script de reemplazo sobre `src/` del servicio.
2. `git mv` de ficheros y directorios de paquete.
3. `mvn -pl <servicio> clean install -DskipTests` → arreglar solo lo que falle.
4. `mvn -pl <servicio> test` → arreglar solo lo que falle.
5. Commit: `git commit -m "rename <servicio> to english"`.

**Atención en `delivery-contracts`:** si la capa 2 se pospone, las **clases** pueden renombrarse (`PedidoEventMessage → OrderEventMessage`) pero los **valores** de `RoutingKeys` y los **nombres de campo JSON / valores de enum serializados** deben quedar como están (usar `@JsonProperty` si un campo se renombra en Java). Esto mantiene compatibilidad binaria de mensajes y de datos Mongo. Los renombrados de clases en contracts son minor si no cambia lo serializado — versionar como `1.4.0`.

Trampas conocidas (documentarlas al arreglar, no buscarlas de antemano):

- `pago` está contenido en `pagado`; `PENDIENTE_DEVOLUCION` mezcla dos términos; `comercioId` es claim del JWT y campo Mongo (capa 2 — **no tocar el string serializado**).
- `PERSONAL` (tipo de usuario) → `STAFF`, pero el valor del enum viaja en JWT y BD: en capa 1 solo se renombra si se mantiene el valor serializado.
- Los `fecha*` del Pedido (`fechaCreacion`, `fechaPago`…) → convención `createdAt`, `paidAt`… — son campos Mongo: en Java sí, en el documento solo con `@Field` apuntando al nombre viejo (o posponer a capa 2).
- MapStruct regenera mappers al compilar: los errores de mapper tras el renombrado se arreglan recompilando, no editando código generado.

## Fase 2 — Textos de usuario (~20–30k tokens)

Mensajes de excepción, textos de email (`LoggingEmailAdapter`), logs y mensajes de validación. Grep por comillas con palabras en español, reemplazo scriptado + repaso manual de los que el script no cubra. No afecta a compilación; verificar con `mvn test` (hay tests que asertan mensajes).

## Fase 3 — Documentación (~30–40k tokens)

`README.md` raíz y de cada servicio, `CLAUDE.md`, `docs/DISENO.md` y los 4 `.puml`. Aquí el reemplazo scriptado sirve de primera pasada, pero la prosa hay que reescribirla (traducción real, no sustitución). Es la única fase donde leer ficheros enteros está justificado.

## Fase 4 (OPCIONAL — decidir explícitamente) — Contratos externos

Solo si se quiere el sistema 100% en inglés por fuera:

- Routing keys, exchange, nombres de cola → **mensajes en vuelo se pierden**: hacer con el sistema parado y colas vacías.
- Campos JSON de la API + valores de enums serializados → invalida las colecciones Postman (regenerarlas) y cualquier cliente.
- Nombres de BD (`pedido_db` → `order_db`) y colecciones → exige migración de datos Mongo o empezar con datos vacíos.
- `delivery-contracts` → **2.0.0**.

Coste adicional estimado: 80–120k tokens + la migración de datos. Alternativa razonable: no hacerla nunca — la frontera externa en español es un contrato estable y documentado.

## Glosario base (semilla de `migration-glossary.txt`)

Dominio: `pedido=order`, `comercio=merchant`, `pago=payment`, `cliente=customer`, `usuario=user`, `repartidor=courier`, `cupon=coupon`, `fidelidad=loyalty`, `puntos=points`, `devolucion=refund`, `suscripcion=subscription`, `notificacion=notification`, `producto=product`, `carrito=cart`, `precio=price`, `cantidad=quantity`, `contrasena=password`, `numeroPedido=orderCode`.

Estados: `PENDIENTE=PENDING`, `ACEPTADO=ACCEPTED`, `RECHAZADO=REJECTED`, `PAGADO=PAID`, `ENTREGADO=DELIVERED`, `CANCELADO=CANCELLED`, `PENDIENTE_DEVOLUCION=REFUND_PENDING`, `DEVUELTO=REFUNDED`, `ACTIVO=ACTIVE`, `ANULADO=VOIDED`, `CADUCADO=EXPIRED`, `ACTIVA=ACTIVE`, `EN_GRACIA=IN_GRACE`, `SUSPENDIDA=SUSPENDED`, `MENSUAL=MONTHLY`, `ANUAL=YEARLY`, `PERSONAL=STAFF`.

Verbos/acciones: `crear=create`, `aceptar=accept`, `rechazar=reject`, `cancelar=cancel`, `anular=void`, `entregar=deliver`, `canjear=redeem`, `reservar=reserve`, `consolidar=consolidate`, `devolver=refund`, `solicitado=requested`, `completado=completed`, `fallido=failed`, `fecha=date` (ver convención `*At` en fase 1).

El glosario se completa durante la fase 1 con lo que vaya apareciendo — añadir cada término nuevo al fichero, no resolverlo a mano en el sitio.

## Resumen de presupuesto

| Fase | Tokens aprox. |
|---|---|
| 0 — Preparación | ~5k |
| 1 — Código interno (7 módulos) | ~280–350k |
| 2 — Textos | ~20–30k |
| 3 — Documentación | ~30–40k |
| 4 — Contratos externos (opcional) | +80–120k |

Las fases son independientes y cada una deja el build en verde: se puede parar después de cualquiera y retomar en otra sesión sin contexto previo — este documento es el punto de entrada.
