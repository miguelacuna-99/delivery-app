import { Injectable, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { DecodedToken, LoginRequest, LoginResponse, TipoUsuario } from '../models/auth.model';
import { TokenStorageService } from './token-storage.service';
import { AuthApiService } from '../../features/auth/services/auth-api.service';
import { firstValueFrom } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly currentUserSignal = signal<DecodedToken | null>(null);

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);
  readonly tipo = computed<TipoUsuario | null>(() => this.currentUserSignal()?.tipo ?? null);
  readonly comercioId = computed<string | null>(() => this.currentUserSignal()?.comercioId ?? null);
  readonly username = computed<string | null>(() => this.currentUserSignal()?.username ?? null);

  constructor(
    private readonly tokenStorage: TokenStorageService,
    private readonly authApi: AuthApiService,
    private readonly router: Router
  ) {}

  async login(request: LoginRequest): Promise<LoginResponse> {
    const response = await firstValueFrom(this.authApi.login(request));
    this.tokenStorage.setToken(response.token);
    const decoded = this.tokenStorage.decode(response.token);
    this.currentUserSignal.set(decoded);
    return response;
  }

  logout(): void {
    this.tokenStorage.clearToken();
    this.currentUserSignal.set(null);
  }

  logoutAndRedirect(expired = false): void {
    this.logout();
    this.router.navigate(['/login'], expired ? { queryParams: { expired: 1 } } : undefined);
  }

  restoreSession(): void {
    const token = this.tokenStorage.getToken();
    if (!token) {
      this.currentUserSignal.set(null);
      return;
    }
    const decoded = this.tokenStorage.decode(token);
    if (!decoded || this.tokenStorage.isExpired(decoded)) {
      this.tokenStorage.clearToken();
      this.currentUserSignal.set(null);
      return;
    }
    this.currentUserSignal.set(decoded);
  }
}
