# Frontend — plan de ejecución y pendientes

Documento vivo (mismo espíritu que `migration-eng.md`): se actualiza a medida que se construye, no se agota en una sesión. Ver también el plan original en `docs/` no aplica aquí — este fichero es la referencia de estado del frontend.

## Arquitectura

Dos proyectos Angular 19 independientes (no un workspace multi-proyecto), carpetas hermanas de los servicios backend:

- `cliente-front/` — rol `CLIENTE`.
- `admin-front/` — roles `ROOT/ADMIN/PERSONAL/REPARTIDOR` (un único dashboard, navegación condicionada por `tipo`).

Ambos hablan con el backend a través de un **gateway nginx** (`gateway/`, puerto host `8080`) que resuelve CORS de forma centralizada y enruta por prefijo de path sin reescritura — el JWT lo sigue validando cada microservicio contra `auth-service /auth/validate`, el gateway no lo toca.

Angular CLI fijado a **v19** (`@angular/cli@19`) en vez de `latest` (v22 en el momento de escribir esto): la CLI más reciente exige Node `^24.15.0` y el Node instalado en esta máquina es `v24.14.1`. v19 cumple "Angular 19+" tal cual se pidió y sus requisitos de Node (`>=22.0.0`) sí encajan con la versión instalada, sin tocar el Node del sistema. Si en el futuro se actualiza Node a `>=24.15.0`, se puede subir de major sin problema (standalone/signals/control flow son estables desde v17).

## Estado por feature

### cliente-front

| Feature | Estado | Qué falta |
|---|---|---|
| auth (login, registro) | Funcional | — |
| catálogo (comercios, productos) | Bandera | — |
| carrito | Bandera | — |
| checkout | Bandera | — |
| mis pedidos (timeline + polling) | Bandera | — |
| contacto | Andamiaje | Validación de formulario, manejo de errores fino |
| password-recovery (forgot/reset) | Andamiaje | Solo verificable mirando logs de `notificacion-service` (no hay SMTP real) |
| puntos (saldo + historial) | Andamiaje | Pulido visual, paginación de historial si crece |

### admin-front

| Feature | Estado | Qué falta |
|---|---|---|
| auth (login) | Funcional | — |
| dashboard | Funcional | — |
| bandeja de pedidos (aceptar/rechazar/entregar/anular) | Bandera | — |
| notificaciones (polling + marcar leída) | Bandera | — |
| ficha de comercio (ver/editar) | Andamiaje | Validación de formulario |
| productos (crear/editar/eliminar) | Andamiaje | Tabla con paginación, manejo de 409 si hay pedidos abiertos con ese producto |
| cupones (crear/anular/listar) | Andamiaje | Validación de fecha de caducidad en el form |
| alta de usuarios | Andamiaje | Restringir `tipo` seleccionable según `tipo` del usuario logueado (ROOT ve todos, ADMIN solo PERSONAL/REPARTIDOR) — la llamada ya lo hace cumplir el backend con 403, falta reflejarlo en el form |

## Diseño visual

Aplicado sobre ambas apps con un sistema de tokens propio (`src/styles/_tokens.scss` en cada proyecto, custom properties CSS, valores idénticos en las dos apps): azul (`--color-blue-600`) como color de acción, amarillo (`--color-yellow-400`) como acento de marca, neutros off-black/off-white, botones/badges en pill (`--radius-full`), tarjetas/inputs en radio medio (`--radius-md`/`--radius-sm`), animaciones de botón (hover con elevación, active con "empuje" `scale(0.97)`) respetando `prefers-reduced-motion`. Sin fotos de producto/comercio (el backend no tiene URLs de imagen): se usan tiles de iniciales con color determinista por id en su lugar, en vez de imágenes de stock falsas.

La skill `design-taste-frontend` está pensada para landing pages en React/Tailwind/Motion/GSAP y excluye explícitamente paneles de administración (`admin-front` es justo eso). Se usaron solo sus principios agnósticos de stack (contraste AA, consistencia de forma/color, animación con propósito, nada de imágenes/iconos falsos) aplicados a mano sobre Angular/SCSS, no sus patrones de código React.

**Bug de contraste real encontrado y corregido**: la primera receta de color para `badge-estado-pedido` (texto saturado `--color-*-600` sobre fondo pastel `--color-*-100` del mismo tono) fallaba WCAG AA en 3 de los 8 estados de pedido (PENDIENTE ~2.1:1, PAGADO ~3.1:1, RECHAZADO ~4.0:1 — el mínimo AA es 4.5:1). Corregido en ambas apps: el texto del badge usa siempre `--color-ink-950` (15-17:1 en los 8 estados) y el color semántico se conserva en un punto de acento (`.badge-estado__dot`) en vez de en el texto.

## Paquete npm compartido (pendiente, NO hacer todavía)

`core/` (interceptor JWT, guards, `AuthService`, `TokenStorageService`, modelos de error) y `shared/` (botón, input, card, badge de estado, spinner, toast) están **duplicados byte a byte** entre `cliente-front` y `admin-front`. El trigger para extraerlos a un paquete (`@delivery/frontend-shared`, análogo a `delivery-contracts` en el backend) es **la aparición de una tercera app Angular** (p. ej. una app dedicada a REPARTIDOR). No hacerlo antes — sería sobre-ingeniería para 2 apps.

## Limitaciones conocidas

- **Sin refresh token**: el JWT expira a las 24h en duro. El interceptor fuerza logout + redirect a `/login` en el primer 401 tras expirar; no hay aviso previo al usuario.
- **Sin WebSocket/push en todo el backend**: todo lo "en tiempo real" (bandeja de pedidos, timeline de pedido del cliente, notificaciones) es polling con intervalo fijo (`shared/constants/polling.constants.ts` en cada app — 8-10s pedidos, 15s notificaciones). Coste conocido: latencia hasta el intervalo configurado y carga extra en los servicios si se despliega a muchos usuarios simultáneos.
- **El gateway no valida el JWT** (por diseño) — lo sigue haciendo cada microservicio vía `auth-service`. Si `auth-service` cae, el resto de servicios empieza a devolver error de validación en los endpoints protegidos.
- **`apiBaseUrl` fijo a `http://localhost:8080`** en `environment.ts` y `environment.development.ts` de ambas apps — no apto para desplegar en un dominio real sin introducir config runtime (`config.json` cargado en bootstrap). Pendiente si se despliega fuera de local/Docker Compose.
- **Repartidor** solo tiene una pantalla dedicada ("marcar entregado") dentro de `admin-front` — no se justifica una app separada con la superficie de API actual (un único endpoint).
