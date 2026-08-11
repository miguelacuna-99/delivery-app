import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Carrito, CarritoRequest } from '../models/carrito.model';

@Injectable({ providedIn: 'root' })
export class CarritoApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/carrito`;

  obtener(): Observable<Carrito> {
    return this.http.get<Carrito>(this.baseUrl);
  }

  actualizar(carrito: CarritoRequest): Observable<Carrito> {
    return this.http.put<Carrito>(this.baseUrl, carrito);
  }

  vaciar(): Observable<void> {
    return this.http.delete<void>(this.baseUrl);
  }
}
