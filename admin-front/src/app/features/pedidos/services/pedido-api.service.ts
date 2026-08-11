import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  AceptarPedidoRequest,
  AnularPedidoRequest,
  EntregarPedidoRequest,
  EstadoPedido,
  Pedido,
  RechazarPedidoRequest
} from '../models/pedido.model';

@Injectable({ providedIn: 'root' })
export class PedidoApiService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/pedidos`;

  constructor(private readonly http: HttpClient) {}

  listar(estado: EstadoPedido): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(this.baseUrl, { params: { estado } });
  }

  aceptar(id: string, request: AceptarPedidoRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/aceptar`, request);
  }

  rechazar(id: string, request: RechazarPedidoRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/rechazar`, request);
  }

  anular(id: string, request: AnularPedidoRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/anular`, request);
  }

  entregar(request: EntregarPedidoRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/entregar`, request);
  }
}
