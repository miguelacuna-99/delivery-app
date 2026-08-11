import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PuntosResponse } from '../models/puntos.model';

@Injectable({ providedIn: 'root' })
export class PuntosApiService {
  private readonly http = inject(HttpClient);

  misPuntos(): Observable<PuntosResponse> {
    return this.http.get<PuntosResponse>(`${environment.apiBaseUrl}/api/puntos/me`);
  }
}
