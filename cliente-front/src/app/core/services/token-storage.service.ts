import { Injectable } from '@angular/core';
import { DecodedToken } from '../models/auth.model';

const TOKEN_KEY = 'df_token';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  set(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  get(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
  }

  /**
   * Decodifica el payload de un JWT estandar (base64url) sin libreria externa.
   * Devuelve null si el token esta mal formado.
   */
  decode(token: string): DecodedToken | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        return null;
      }

      const payload = this.base64UrlDecode(parts[1]);
      const parsed = JSON.parse(payload) as Record<string, unknown>;

      const userId = (parsed['userId'] ?? parsed['sub']) as string | undefined;
      const username = parsed['username'] as string | undefined;
      const tipo = parsed['tipo'] as DecodedToken['tipo'] | undefined;
      const exp = parsed['exp'] as number | undefined;

      if (!userId || !username || !tipo || !exp) {
        return null;
      }

      return {
        userId,
        username,
        comercioId: (parsed['comercioId'] as string | null | undefined) ?? null,
        tipo,
        exp
      };
    } catch {
      return null;
    }
  }

  isExpired(decoded: DecodedToken | null): boolean {
    if (!decoded) {
      return true;
    }
    const nowSeconds = Math.floor(Date.now() / 1000);
    return decoded.exp <= nowSeconds;
  }

  private base64UrlDecode(input: string): string {
    let base64 = input.replace(/-/g, '+').replace(/_/g, '/');
    const padding = base64.length % 4;
    if (padding === 2) {
      base64 += '==';
    } else if (padding === 3) {
      base64 += '=';
    }
    const binary = atob(base64);
    const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0));
    return new TextDecoder('utf-8').decode(bytes);
  }
}
