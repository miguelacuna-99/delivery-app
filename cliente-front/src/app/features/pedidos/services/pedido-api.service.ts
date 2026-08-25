import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Pedido } from '../models/pedido.model';

@Injectable({ providedIn: 'root' })
export class PedidoApiService {
  private readonly http = inject(HttpClient);

  misPedidos(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${environment.apiBaseUrl}/api/pedidos/me`);
  }

  pagar(pedidoId: string, tarjetaId: string): Observable<Pedido> {
    return this.http.post<Pedido>(`${environment.apiBaseUrl}/api/pedidos/${pedidoId}/pagar`, { tarjetaId });
  }
}
