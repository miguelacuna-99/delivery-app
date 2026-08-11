import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface Comercio {
  id: string;
  nombre: string;
  direccion: string;
  telefono: string;
  email: string;
  [key: string]: unknown;
}

export interface ActualizarComercioRequest {
  nombre?: string;
  direccion?: string;
  telefono?: string;
  email?: string;
}

@Injectable({ providedIn: 'root' })
export class ComercioApiService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/comercios`;

  constructor(private readonly http: HttpClient) {}

  obtenerMiComercio(): Observable<Comercio> {
    return this.http.get<Comercio>(`${this.baseUrl}/me`);
  }

  actualizarMiComercio(request: ActualizarComercioRequest): Observable<Comercio> {
    return this.http.put<Comercio>(`${this.baseUrl}/me`, request);
  }
}
