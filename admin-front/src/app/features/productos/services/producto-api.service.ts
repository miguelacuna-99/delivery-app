import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth.service';

export interface Producto {
  id: string;
  nombre: string;
  descripcion: string;
  precio: number;
  disponible: boolean;
  [key: string]: unknown;
}

export interface CrearProductoRequest {
  nombre: string;
  descripcion: string;
  precio: number;
  disponible: boolean;
}

export type ActualizarProductoRequest = Partial<CrearProductoRequest>;

@Injectable({ providedIn: 'root' })
export class ProductoApiService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/productos`;
  private readonly authService = inject(AuthService);

  constructor(private readonly http: HttpClient) {}

  // No existe GET /api/productos: el catálogo se lee del endpoint público
  // de comercio-service (el mismo que usa cliente-front), filtrado por el
  // comercioId del usuario logueado.
  listar(): Observable<Producto[]> {
    const comercioId = this.authService.comercioId();
    return this.http.get<Producto[]>(`${environment.apiBaseUrl}/api/comercios/${comercioId}/productos`);
  }

  crear(request: CrearProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(this.baseUrl, request);
  }

  actualizar(id: string, request: ActualizarProductoRequest): Observable<Producto> {
    return this.http.put<Producto>(`${this.baseUrl}/${id}`, request);
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
