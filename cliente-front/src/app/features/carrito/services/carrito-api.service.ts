import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AplicarCuponResponse, AplicarPuntosResponse, Carrito, CarritoRequest } from '../models/carrito.model';

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

  aplicarCupon(codigo: string): Observable<AplicarCuponResponse> {
    return this.http.post<AplicarCuponResponse>(`${this.baseUrl}/cupon`, { codigo });
  }

  quitarCupon(): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/cupon`);
  }

  aplicarPuntos(puntos: number): Observable<AplicarPuntosResponse> {
    return this.http.post<AplicarPuntosResponse>(`${this.baseUrl}/puntos`, { puntos });
  }

  quitarPuntos(): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/puntos`);
  }
}
