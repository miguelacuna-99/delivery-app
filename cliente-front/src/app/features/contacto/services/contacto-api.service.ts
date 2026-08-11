import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ClienteResponse } from '../../../core/models/auth.model';

export interface ActualizarContactoRequest {
  mail?: string;
  direccionDomicilio?: string;
  telefono?: string;
}

@Injectable({ providedIn: 'root' })
export class ContactoApiService {
  private readonly http = inject(HttpClient);

  actualizar(req: ActualizarContactoRequest): Observable<ClienteResponse> {
    return this.http.put<ClienteResponse>(`${environment.apiBaseUrl}/api/clientes/me/contacto`, req);
  }
}
