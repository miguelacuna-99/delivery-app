import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Comercio } from '../models/comercio.model';
import { Producto } from '../models/producto.model';

export interface ComercioApiPort {
  listar(): Observable<Comercio[]>;
  obtener(comercioId: string): Observable<Comercio>;
  catalogo(comercioId: string): Observable<Producto[]>;
}

@Injectable({ providedIn: 'root' })
export class ComercioApiService implements ComercioApiPort {
  private readonly http = inject(HttpClient);

  listar(): Observable<Comercio[]> {
    return this.http.get<Comercio[]>(`${environment.apiBaseUrl}/api/comercios`);
  }

  obtener(comercioId: string): Observable<Comercio> {
    return this.http.get<Comercio>(`${environment.apiBaseUrl}/api/comercios/${comercioId}`);
  }

  catalogo(comercioId: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${environment.apiBaseUrl}/api/comercios/${comercioId}/productos`);
  }
}
