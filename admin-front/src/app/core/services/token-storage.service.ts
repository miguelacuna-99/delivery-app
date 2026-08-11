import { Injectable } from '@angular/core';
import { DecodedToken } from '../models/auth.model';

const TOKEN_KEY = 'df_token';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  clearToken(): void {
    localStorage.removeItem(TOKEN_KEY);
  }

  decode(token: string): DecodedToken | null {
    try {
      const payload = token.split('.')[1];
      if (!payload) {
        return null;
      }
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
      const json = decodeURIComponent(
        atob(padded)
          .split('')
          .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
          .join('')
      );
      const parsed = JSON.parse(json) as Record<string, unknown>;
      // El JWT real usa el claim estandar "sub" para el userId, no "userId".
      return {
        userId: (parsed['userId'] ?? parsed['sub']) as string,
        username: parsed['username'] as string,
        comercioId: (parsed['comercioId'] as string | null | undefined) ?? null,
        tipo: parsed['tipo'] as DecodedToken['tipo'],
        exp: parsed['exp'] as number
      };
    } catch {
      return null;
    }
  }

  isExpired(decoded: DecodedToken | null): boolean {
    if (!decoded || !decoded.exp) {
      return true;
    }
    const nowSeconds = Math.floor(Date.now() / 1000);
    return decoded.exp <= nowSeconds;
  }
}
