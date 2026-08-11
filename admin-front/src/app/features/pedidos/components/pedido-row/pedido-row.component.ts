import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EstadoPedido, Pedido } from '../../models/pedido.model';
import { BadgeEstadoPedidoComponent } from '../../../../shared/components/badge-estado-pedido/badge-estado-pedido.component';
import { ButtonComponent } from '../../../../shared/components/button/button.component';

@Component({
  selector: 'app-pedido-row',
  standalone: true,
  imports: [CommonModule, BadgeEstadoPedidoComponent, ButtonComponent],
  template: `
    <tr>
      <td>{{ pedido.numeroPedido }}</td>
      <td>
        <app-badge-estado-pedido [estado]="pedido.estado"></app-badge-estado-pedido>
      </td>
      <td>{{ pedido.items.length }} items</td>
      <td>{{ pedido.total | number: '1.2-2' }} €</td>
      <td>{{ pedido.fechaCreacion | date: 'short' }}</td>
      <td class="pedido-row__actions">
        @if (canGestionar && pedido.estado === estadoPendiente) {
          <app-button variant="primary" (clicked)="aceptar.emit()">Aceptar</app-button>
          <app-button variant="danger" (clicked)="rechazar.emit()">Rechazar</app-button>
        }
        @if (canAnular && pedido.estado === estadoPagado) {
          <app-button variant="ghost" (clicked)="anular.emit()">Anular</app-button>
        }
      </td>
    </tr>
  `,
  styleUrl: './pedido-row.component.scss'
})
export class PedidoRowComponent {
  @Input({ required: true }) pedido!: Pedido;
  @Input() canGestionar = false;
  @Input() canAnular = false;

  @Output() aceptar = new EventEmitter<void>();
  @Output() rechazar = new EventEmitter<void>();
  @Output() anular = new EventEmitter<void>();

  readonly estadoPendiente = EstadoPedido.PENDIENTE;
  readonly estadoPagado = EstadoPedido.PAGADO;
}
