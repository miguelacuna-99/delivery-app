import { DatePipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { Pedido } from '../../models/pedido.model';

export type PasoEstado = 'completado' | 'actual' | 'pendiente' | 'error';

export interface PasoTimeline {
  label: string;
  fecha: string | null;
  estado: PasoEstado;
}

/**
 * Deriva los pasos de la linea de tiempo de un pedido a partir de sus 8
 * fechas + el estado actual. No hace llamadas HTTP: solo pinta lo que le
 * llega por input.
 */
@Component({
  selector: 'app-pedido-timeline',
  imports: [DatePipe],
  templateUrl: './pedido-timeline.component.html',
  styleUrl: './pedido-timeline.component.scss'
})
export class PedidoTimelineComponent {
  readonly pedido = input.required<Pedido>();

  readonly pasos = computed<PasoTimeline[]>(() => this.calcularPasos(this.pedido()));

  private calcularPasos(pedido: Pedido): PasoTimeline[] {
    if (pedido.estado === 'RECHAZADO') {
      return [
        this.paso('Creado', pedido.fechaCreacion, 'completado'),
        this.paso('Rechazado', pedido.fechaRechazo, 'error')
      ];
    }

    if (pedido.estado === 'CANCELADO') {
      return [
        this.paso('Creado', pedido.fechaCreacion, 'completado'),
        this.paso('Aceptado', pedido.fechaAceptacion, pedido.fechaAceptacion ? 'completado' : 'pendiente'),
        this.paso('Cancelado', pedido.fechaCancelacion, 'error')
      ];
    }

    const pasos: PasoTimeline[] = [
      this.pasoAutomatico('Creado', pedido.fechaCreacion),
      this.pasoAutomatico('Aceptado', pedido.fechaAceptacion),
      this.pasoAutomatico('Pagado', pedido.fechaPago),
      this.pasoAutomatico('Entregado', pedido.fechaEntrega)
    ];

    this.marcarPasoActual(pasos);

    if (pedido.estado === 'PENDIENTE_DEVOLUCION' || pedido.estado === 'DEVUELTO') {
      pasos.push(
        this.paso(
          'Devuelto',
          pedido.fechaDevolucion,
          pedido.estado === 'DEVUELTO' ? 'completado' : 'actual'
        )
      );
    }

    return pasos;
  }

  private pasoAutomatico(label: string, fecha: string | null): PasoTimeline {
    return this.paso(label, fecha, fecha ? 'completado' : 'pendiente');
  }

  private paso(label: string, fecha: string | null, estado: PasoEstado): PasoTimeline {
    return { label, fecha, estado };
  }

  /** Marca como "actual" el primer paso pendiente inmediatamente despues del ultimo completado. */
  private marcarPasoActual(pasos: PasoTimeline[]): void {
    const primerPendiente = pasos.findIndex((paso) => paso.estado === 'pendiente');
    if (primerPendiente === -1) {
      return;
    }
    const anterior = pasos[primerPendiente - 1];
    if (primerPendiente === 0 || anterior?.estado === 'completado') {
      pasos[primerPendiente] = { ...pasos[primerPendiente], estado: 'actual' };
    }
  }
}
