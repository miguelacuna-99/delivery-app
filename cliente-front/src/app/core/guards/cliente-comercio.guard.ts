import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TipoUsuario } from '../models/auth.model';

/**
 * Cada cliente esta atado a un unico comercio desde su registro (ver
 * ClienteService.register en auth-service). Un cliente logueado nunca deberia
 * navegar el listado multi-comercio ni el catalogo de otro negocio: se le
 * redirige siempre al suyo. Visitantes sin sesion (o tipos no-CLIENTE) pasan
 * sin tocar nada, para que el listado publico siga sirviendo de base al
 * desplegable de registro.
 */
export const clienteComercioGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated() || authService.tipo() !== TipoUsuario.CLIENTE) {
    return true;
  }

  const suComercioId = authService.comercioId();
  if (!suComercioId) {
    return true;
  }

  const comercioIdRuta = route.paramMap.get('comercioId');
  if (comercioIdRuta === null || comercioIdRuta !== suComercioId) {
    return router.createUrlTree(['/catalogo', suComercioId]);
  }

  return true;
};
