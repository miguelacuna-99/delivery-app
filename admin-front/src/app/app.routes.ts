import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { TipoUsuario } from './core/models/auth.model';
import { AuthenticatedShellComponent } from './layout/authenticated-shell/authenticated-shell.component';
import { LoginPageComponent } from './features/auth/pages/login-page/login-page.component';
import { DashboardPageComponent } from './features/dashboard/pages/dashboard-page/dashboard-page.component';
import { BandejaPedidosPageComponent } from './features/pedidos/pages/bandeja-pedidos-page/bandeja-pedidos-page.component';
import { EntregarPedidoPageComponent } from './features/pedidos/pages/entregar-pedido-page/entregar-pedido-page.component';
import { NotificacionesPageComponent } from './features/notificaciones/pages/notificaciones-page/notificaciones-page.component';
import { MiComercioPageComponent } from './features/comercio/pages/mi-comercio-page/mi-comercio-page.component';
import { ProductosPageComponent } from './features/productos/pages/productos-page/productos-page.component';
import { CuponesPageComponent } from './features/cupones/pages/cupones-page/cupones-page.component';
import { AltaUsuarioPageComponent } from './features/usuarios/pages/alta-usuario-page/alta-usuario-page.component';

export const routes: Routes = [
  { path: 'login', component: LoginPageComponent },
  {
    path: '',
    component: AuthenticatedShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardPageComponent },
      {
        path: 'pedidos',
        component: BandejaPedidosPageComponent,
        canActivate: [roleGuard([TipoUsuario.ROOT, TipoUsuario.ADMIN, TipoUsuario.PERSONAL])]
      },
      {
        path: 'pedidos/entregar',
        component: EntregarPedidoPageComponent,
        canActivate: [roleGuard([TipoUsuario.REPARTIDOR])]
      },
      {
        path: 'notificaciones',
        component: NotificacionesPageComponent,
        canActivate: [roleGuard([TipoUsuario.ROOT, TipoUsuario.ADMIN, TipoUsuario.PERSONAL])]
      },
      { path: 'comercio', component: MiComercioPageComponent },
      {
        path: 'productos',
        component: ProductosPageComponent,
        canActivate: [roleGuard([TipoUsuario.ROOT, TipoUsuario.ADMIN])]
      },
      {
        path: 'cupones',
        component: CuponesPageComponent,
        canActivate: [roleGuard([TipoUsuario.ROOT, TipoUsuario.ADMIN])]
      },
      {
        path: 'usuarios',
        component: AltaUsuarioPageComponent,
        canActivate: [roleGuard([TipoUsuario.ROOT, TipoUsuario.ADMIN])]
      }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
