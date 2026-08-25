import { Injectable, computed, signal } from '@angular/core';
import { DecodedToken, LoginResponse, TipoUsuario } from '../models/auth.model';
import { TokenStorageService } from './token-storage.service';

/**
 * Estado de sesion de la aplicacion, basado en signals.
 *
 * No hace peticiones HTTP: eso vive en features/auth/services/auth-api.service.ts.
 * Este servicio solo gestiona el token (persistencia + decodificacion) y el
 * estado reactivo de "quien esta logueado ahora mismo".
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly currentUserSignal = signal<DecodedToken | null>(null);

  readonly currentUser = this.currentUserSignal.asReadonly();

  readonly isAuthenticated = computed(() => {
    const user = this.currentUserSignal();
    return user !== null && !this.tokenStorage.isExpired(user);
  });

  readonly tipo = computed<TipoUsuario | null>(() => this.currentUserSignal()?.tipo ?? null);
  readonly comercioId = computed<string | null>(() => this.currentUserSignal()?.comercioId ?? null);

  constructor(private readonly tokenStorage: TokenStorageService) {}

  /**
   * Guarda la sesion a partir de la respuesta de POST /auth/login.
   */
  login(response: LoginResponse): void {
    this.tokenStorage.set(response.token);
    const decoded = this.tokenStorage.decode(response.token);
    this.currentUserSignal.set(decoded);
  }

  logout(): void {
    this.tokenStorage.clear();
    this.currentUserSignal.set(null);
  }

  /**
   * Se llama una vez al arrancar la app (provideAppInitializer) para recuperar
   * la sesion desde localStorage si el token sigue siendo valido.
   */
  restoreSession(): void {
    const token = this.tokenStorage.get();
    if (!token) {
      this.currentUserSignal.set(null);
      return;
    }

    const decoded = this.tokenStorage.decode(token);
    if (!decoded || this.tokenStorage.isExpired(decoded)) {
      this.tokenStorage.clear();
      this.currentUserSignal.set(null);
      return;
    }

    this.currentUserSignal.set(decoded);
  }
}
