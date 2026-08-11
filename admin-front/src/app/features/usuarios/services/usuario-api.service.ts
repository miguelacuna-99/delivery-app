import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { TipoUsuario } from '../../../core/models/auth.model';

export interface CrearUsuarioRequest {
  username: string;
  password: string;
  mail: string;
  telefono: string;
  tipo: TipoUsuario;
}

export interface Usuario {
  id: string;
  username: string;
  mail: string;
  telefono: string;
  tipo: TipoUsuario;
  [key: string]: unknown;
}

@Injectable({ providedIn: 'root' })
export class UsuarioApiService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/usuarios`;

  constructor(private readonly http: HttpClient) {}

  crear(request: CrearUsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.baseUrl, request);
  }
}
