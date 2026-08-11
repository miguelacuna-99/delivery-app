import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { interval, of } from 'rxjs';
import { catchError, startWith, switchMap, tap } from 'rxjs/operators';
import { PedidoApiService } from '../../services/pedido-api.service';
import { Pedido } from '../../models/pedido.model';
import { toAppHttpError } from '../../../../core/models/http-error.model';
import { PEDIDOS_POLLING_MS } from '../../../../shared/constants/polling.constants';
import { PedidoCardComponent } from '../../components/pedido-card/pedido-card.component';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';

@Component({
  selector: 'app-mis-pedidos-page',
  imports: [PedidoCardComponent, SpinnerComponent],
  templateUrl: './mis-pedidos-page.component.html',
  styleUrl: './mis-pedidos-page.component.scss'
})
export class MisPedidosPageComponent {
  private readonly pedidoApi = inject(PedidoApiService);
  private readonly route = inject(ActivatedRoute);

  readonly error = signal<string | null>(null);
  readonly cargandoPrimeraVez = signal(true);
  readonly nuevoId = this.route.snapshot.queryParamMap.get('nuevo');

  private readonly pedidos$ = interval(PEDIDOS_POLLING_MS).pipe(
    startWith(0),
    switchMap(() =>
      this.pedidoApi.misPedidos().pipe(
        tap(() => {
          this.error.set(null);
          this.cargandoPrimeraVez.set(false);
        }),
        catchError((err) => {
          this.error.set(toAppHttpError(err).message);
          this.cargandoPrimeraVez.set(false);
          return of<Pedido[]>([]);
        })
      )
    ),
    takeUntilDestroyed()
  );

  readonly pedidos = toSignal(this.pedidos$, { initialValue: [] as Pedido[] });
}
