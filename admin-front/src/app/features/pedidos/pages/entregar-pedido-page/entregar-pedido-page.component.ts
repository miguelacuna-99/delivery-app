import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PedidoApiService } from '../../services/pedido-api.service';
import { InputComponent } from '../../../../shared/components/input/input.component';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { IconComponent } from '../../../../shared/components/icon/icon.component';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-entregar-pedido-page',
  standalone: true,
  imports: [CommonModule, InputComponent, ButtonComponent, CardComponent, IconComponent],
  template: `
    <h2 class="page-title">Entregar pedido</h2>
    <app-card class="entregar-page__card form-card">
      <div class="entregar-page__icon">
        <app-icon name="entregar" [size]="26"></app-icon>
      </div>
      <p class="entregar-page__hint">
        Introduce el número que te ha dado el cliente para marcar el pedido como entregado.
      </p>
      <app-input
        label="Número de pedido"
        [value]="numeroPedido()"
        (valueChange)="numeroPedido.set($event)"
        placeholder="Ej. 4F82K"
      ></app-input>
      <app-button [loading]="loading()" (clicked)="entregar()">Marcar como entregado</app-button>
    </app-card>
  `,
  styleUrl: './entregar-pedido-page.component.scss'
})
export class EntregarPedidoPageComponent {
  private readonly pedidoApi = inject(PedidoApiService);
  private readonly toastService = inject(ToastService);

  readonly numeroPedido = signal('');
  readonly loading = signal(false);

  entregar(): void {
    if (!this.numeroPedido()) {
      this.toastService.error('Introduce un número de pedido.');
      return;
    }
    this.loading.set(true);
    this.pedidoApi.entregar({ numeroPedido: this.numeroPedido() }).subscribe({
      next: () => {
        this.loading.set(false);
        this.toastService.success('Pedido marcado como entregado.');
        this.numeroPedido.set('');
      },
      error: () => {
        this.loading.set(false);
        this.toastService.error('No se pudo marcar el pedido como entregado.');
      }
    });
  }
}
