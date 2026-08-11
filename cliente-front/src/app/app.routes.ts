import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'catalogo' },
  {
    path: '',
    loadComponent: () =>
      import('./layout/public-shell/public-shell.component').then((m) => m.PublicShellComponent),
    children: [
      {
        path: 'login',
        canActivate: [guestGuard],
        loadComponent: () =>
          import('./features/auth/pages/login-page/login-page.component').then((m) => m.LoginPageComponent)
      },
      {
        path: 'registro',
        canActivate: [guestGuard],
        loadComponent: () =>
          import('./features/auth/pages/registro-page/registro-page.component').then(
            (m) => m.RegistroPageComponent
          )
      },
      {
        path: 'password/olvide',
        loadComponent: () =>
          import('./features/password-recovery/pages/forgot-page/forgot-page.component').then(
            (m) => m.ForgotPageComponent
          )
      },
      {
        path: 'password/reset',
        loadComponent: () =>
          import('./features/password-recovery/pages/reset-page/reset-page.component').then(
            (m) => m.ResetPageComponent
          )
      }
    ]
  },
  {
    path: '',
    loadComponent: () =>
      import('./layout/authenticated-shell/authenticated-shell.component').then(
        (m) => m.AuthenticatedShellComponent
      ),
    children: [
      {
        path: 'catalogo',
        loadComponent: () =>
          import('./features/catalogo/pages/comercios-page/comercios-page.component').then(
            (m) => m.ComerciosPageComponent
          )
      },
      {
        path: 'catalogo/:comercioId',
        loadComponent: () =>
          import('./features/catalogo/pages/comercio-detalle-page/comercio-detalle-page.component').then(
            (m) => m.ComercioDetallePageComponent
          )
      },
      {
        path: 'carrito',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/carrito/pages/carrito-page/carrito-page.component').then(
            (m) => m.CarritoPageComponent
          )
      },
      {
        path: 'checkout',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/checkout/pages/checkout-page/checkout-page.component').then(
            (m) => m.CheckoutPageComponent
          )
      },
      {
        path: 'pedidos',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/pedidos/pages/mis-pedidos-page/mis-pedidos-page.component').then(
            (m) => m.MisPedidosPageComponent
          )
      },
      {
        path: 'puntos',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/puntos/pages/puntos-page/puntos-page.component').then(
            (m) => m.PuntosPageComponent
          )
      },
      {
        path: 'contacto',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/contacto/pages/contacto-page/contacto-page.component').then(
            (m) => m.ContactoPageComponent
          )
      }
    ]
  },
  { path: '**', redirectTo: 'catalogo' }
];
