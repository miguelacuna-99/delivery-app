import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface Cupon {
  id: string;
  codigo: string;
  porcentajeDescuento: number;
  fechaCaducidad: string;
  estado: 'ACTIVO' | 'ANULADO' | 'CADUCADO';
  [key: string]: unknown;
}

export interface CrearCuponRequest {
  codigo: string;
  porcentajeDescuento: number;
  fechaCaducidad: string;
}

@Injectable({ providedIn: 'root' })
export class CuponApiService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/cupones`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<Cupon[]> {
    return this.http.get<Cupon[]>(this.baseUrl);
  }

  crear(request: CrearCuponRequest): Observable<Cupon> {
    return this.http.post<Cupon>(this.baseUrl, request);
  }

  anular(id: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/anular`, {});
  }
}
