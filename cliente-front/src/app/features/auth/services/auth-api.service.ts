import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  ClienteResponse,
  LoginRequest,
  LoginResponse,
  RegistroClienteRequest
} from '../../../core/models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);

  login(req: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, req);
  }

  registro(req: RegistroClienteRequest): Observable<ClienteResponse> {
    return this.http.post<ClienteResponse>(`${environment.apiBaseUrl}/api/clientes/registro`, req);
  }
}
