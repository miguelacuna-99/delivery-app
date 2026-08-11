import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TipoUsuario } from '../models/auth.model';

export const roleGuard = (allowed: TipoUsuario[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const tipo = authService.tipo();
    if (tipo && allowed.includes(tipo)) {
      return true;
    }

    return router.createUrlTree(['/dashboard']);
  };
};
