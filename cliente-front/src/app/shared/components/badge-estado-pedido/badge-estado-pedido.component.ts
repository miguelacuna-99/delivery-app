import { Component, computed, input } from '@angular/core';

interface EstadoVisual {
  texto: string;
  clase: string;
}

const ESTADOS_VISUALES: Record<string, EstadoVisual> = {
  PENDIENTE: { texto: 'Pendiente', clase: 'badge-estado--pendiente' },
  ACEPTADO: { texto: 'Aceptado', clase: 'badge-estado--aceptado' },
  RECHAZADO: { texto: 'Rechazado', clase: 'badge-estado--rechazado' },
  PAGADO: { texto: 'Pagado', clase: 'badge-estado--pagado' },
  ENTREGADO: { texto: 'Entregado', clase: 'badge-estado--entregado' },
  CANCELADO: { texto: 'Cancelado', clase: 'badge-estado--cancelado' },
  PENDIENTE_DEVOLUCION: { texto: 'Devolucion en curso', clase: 'badge-estado--pendiente-devolucion' },
  DEVUELTO: { texto: 'Devuelto', clase: 'badge-estado--devuelto' }
};

const ESTADO_DESCONOCIDO: EstadoVisual = { texto: 'Desconocido', clase: 'badge-estado--desconocido' };

/**
 * Pinta un badge de color+texto para un estado de pedido. No contiene logica
 * de negocio: solo traduce el string del enum EstadoPedido a algo visual.
 */
@Component({
  selector: 'ui-badge-estado-pedido',
  templateUrl: './badge-estado-pedido.component.html',
  styleUrl: './badge-estado-pedido.component.scss'
})
export class BadgeEstadoPedidoComponent {
  readonly estado = input.required<string>();

  protected readonly visual = computed<EstadoVisual>(
    () => ESTADOS_VISUALES[this.estado()] ?? ESTADO_DESCONOCIDO
  );
}
