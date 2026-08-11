import { Component, Input, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EstadoPedido } from '../../../features/pedidos/models/pedido.model';

interface EstadoVisual {
  texto: string;
  clase: string;
}

const ESTADO_VISUALS: Record<EstadoPedido, EstadoVisual> = {
  [EstadoPedido.PENDIENTE]: { texto: 'Pendiente', clase: 'pendiente' },
  [EstadoPedido.ACEPTADO]: { texto: 'Aceptado', clase: 'aceptado' },
  [EstadoPedido.RECHAZADO]: { texto: 'Rechazado', clase: 'rechazado' },
  [EstadoPedido.PAGADO]: { texto: 'Pagado', clase: 'pagado' },
  [EstadoPedido.ENTREGADO]: { texto: 'Entregado', clase: 'pagado' },
  [EstadoPedido.CANCELADO]: { texto: 'Cancelado', clase: 'rechazado' },
  [EstadoPedido.PENDIENTE_DEVOLUCION]: { texto: 'Pend. devolución', clase: 'devuelto' },
  [EstadoPedido.DEVUELTO]: { texto: 'Devuelto', clase: 'devuelto' }
};

/**
 * Fondo pastel por estado (según el sistema de diseño) + texto en --color-ink-950
 * y un punto de acento en el color semántico: con el texto saturado sobre su propio
 * fondo pastel (p.ej. --color-yellow-600 sobre --color-yellow-100) el contraste cae
 * por debajo de 4.5:1 en varios estados (PENDIENTE ~2.1:1, PAGADO ~3.1:1,
 * RECHAZADO ~4.0:1) y no hay un tono más oscuro disponible en la paleta. Usar
 * ink-950 para el texto garantiza AA en los 5 estados sin salirse de los tokens.
 */
@Component({
  selector: 'app-badge-estado-pedido',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="app-badge app-badge--{{ visual().clase }}">
      <span class="app-badge__dot" aria-hidden="true"></span>
      {{ visual().texto }}
    </span>
  `,
  styleUrl: './badge-estado-pedido.component.scss'
})
export class BadgeEstadoPedidoComponent {
  private readonly estadoSignal = signal<EstadoPedido>(EstadoPedido.PENDIENTE);

  @Input() set estado(value: EstadoPedido) {
    this.estadoSignal.set(value);
  }

  readonly visual = computed(() => ESTADO_VISUALS[this.estadoSignal()]);
}
