import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Notificacion, TipoNotificacion } from '../models/notificacion.model';

@Injectable({ providedIn: 'root' })
export class NotificacionApiService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/notificaciones`;

  constructor(private readonly http: HttpClient) {}

  listar(tipo: TipoNotificacion): Observable<Notificacion[]> {
    return this.http.get<Notificacion[]>(this.baseUrl, { params: { tipo } });
  }

  marcarLeida(id: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/leida`, {});
  }
}
