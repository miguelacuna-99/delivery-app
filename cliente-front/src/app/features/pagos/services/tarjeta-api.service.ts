import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { GuardarTarjetaRequest, Tarjeta } from '../models/tarjeta.model';

@Injectable({ providedIn: 'root' })
export class TarjetaApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/tarjetas`;

  listar(): Observable<Tarjeta[]> {
    return this.http.get<Tarjeta[]>(this.baseUrl);
  }

  guardar(request: GuardarTarjetaRequest): Observable<Tarjeta> {
    return this.http.post<Tarjeta>(this.baseUrl, request);
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
