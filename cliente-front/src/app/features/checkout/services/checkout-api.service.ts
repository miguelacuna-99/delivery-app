import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Pedido } from '../../pedidos/models/pedido.model';

@Injectable({ providedIn: 'root' })
export class CheckoutApiService {
  private readonly http = inject(HttpClient);

  checkout(): Observable<Pedido> {
    return this.http.post<Pedido>(`${environment.apiBaseUrl}/api/carrito/checkout`, {});
  }
}
