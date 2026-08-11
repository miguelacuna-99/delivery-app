import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PedidoApiService } from '../../services/pedido-api.service';
import { EstadoPedido, Pedido } from '../../models/pedido.model';
import { PedidoRowComponent } from '../../components/pedido-row/pedido-row.component';
import {
  AceptarPayload,
  AceptarRechazarAccion,
  AceptarRechazarModalComponent,
  RechazarPayload
} from '../../components/aceptar-rechazar-modal/aceptar-rechazar-modal.component';
import { AuthService } from '../../../../core/services/auth.service';
import { TipoUsuario } from '../../../../core/models/auth.model';
import { ToastService } from '../../../../shared/services/toast.service';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { IconComponent } from '../../../../shared/components/icon/icon.component';
import { PEDIDOS_POLLING_MS } from '../../../../shared/constants/polling.constants';

@Component({
  selector: 'app-bandeja-pedidos-page',
  standalone: true,
  imports: [CommonModule, PedidoRowComponent, AceptarRechazarModalComponent, SpinnerComponent, IconComponent],
  template: `
    <h2 class="page-title">Bandeja de pedidos</h2>

    <div class="filter-bar">
      <label for="estado-select">Estado</label>
      <select
        id="estado-select"
        class="filter-select"
        [value]="estadoSeleccionado()"
        (change)="onEstadoChange($event)"
      >
        @for (estado of estados; track estado) {
          <option [value]="estado">{{ estado }}</option>
        }
      </select>
    </div>

    @if (loading()) {
      <div class="loading-state"><app-spinner></app-spinner></div>
    }

    @if (!loading() && pedidos().length === 0) {
      <div class="empty-state">
        <app-icon name="inbox" [size]="32"></app-icon>
        <p>No hay pedidos en estado {{ estadoSeleccionado() }}.</p>
      </div>
    }

    @if (pedidos().length > 0) {
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>Nº pedido</th>
              <th>Estado</th>
              <th>Items</th>
              <th>Total</th>
              <th>Fecha creación</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (pedido of pedidos(); track pedido.id) {
              <app-pedido-row
                [pedido]="pedido"
                [canGestionar]="canGestionar()"
                [canAnular]="canAnular()"
                (aceptar)="abrirModal('aceptar', pedido)"
                (rechazar)="abrirModal('rechazar', pedido)"
                (anular)="anularPedido(pedido)"
              ></app-pedido-row>
            }
          </tbody>
        </table>
      </div>
    }

    <app-aceptar-rechazar-modal
      [visible]="modalVisible()"
      [accion]="modalAccion()"
      (cancelar)="cerrarModal()"
      (confirmarAceptar)="confirmarAceptar($event)"
      (confirmarRechazar)="confirmarRechazar($event)"
    ></app-aceptar-rechazar-modal>
  `,
  styleUrl: './bandeja-pedidos-page.component.scss'
})
export class BandejaPedidosPageComponent {
  private readonly pedidoApi = inject(PedidoApiService);
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  readonly estados = Object.values(EstadoPedido);

  readonly pedidos = signal<Pedido[]>([]);
  readonly loading = signal(false);
  readonly estadoSeleccionado = signal<EstadoPedido>(EstadoPedido.PENDIENTE);

  readonly modalVisible = signal(false);
  readonly modalAccion = signal<AceptarRechazarAccion>('aceptar');
  private pedidoSeleccionado: Pedido | null = null;

  readonly canGestionar = computed(() => {
    const tipo = this.authService.tipo();
    return tipo === TipoUsuario.ROOT || tipo === TipoUsuario.ADMIN || tipo === TipoUsuario.PERSONAL;
  });

  readonly canAnular = computed(() => {
    const tipo = this.authService.tipo();
    return tipo === TipoUsuario.ROOT || tipo === TipoUsuario.ADMIN;
  });

  constructor() {
    this.cargarPedidos();
    const intervalId = setInterval(() => this.cargarPedidos(), PEDIDOS_POLLING_MS);
    this.destroyRef.onDestroy(() => clearInterval(intervalId));
  }

  onEstadoChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as EstadoPedido;
    this.estadoSeleccionado.set(value);
    this.cargarPedidos();
  }

  cargarPedidos(): void {
    this.loading.set(true);
    this.pedidoApi.listar(this.estadoSeleccionado()).subscribe({
      next: (pedidos) => {
        this.pedidos.set(pedidos);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toastService.error('No se pudieron cargar los pedidos.');
      }
    });
  }

  abrirModal(accion: AceptarRechazarAccion, pedido: Pedido): void {
    this.pedidoSeleccionado = pedido;
    this.modalAccion.set(accion);
    this.modalVisible.set(true);
  }

  cerrarModal(): void {
    this.modalVisible.set(false);
    this.pedidoSeleccionado = null;
  }

  confirmarAceptar(payload: AceptarPayload): void {
    if (!this.pedidoSeleccionado) {
      return;
    }
    this.pedidoApi.aceptar(this.pedidoSeleccionado.id, payload).subscribe({
      next: () => {
        this.toastService.success('Pedido aceptado.');
        this.cerrarModal();
        this.cargarPedidos();
      },
      error: () => this.toastService.error('No se pudo aceptar el pedido.')
    });
  }

  confirmarRechazar(payload: RechazarPayload): void {
    if (!this.pedidoSeleccionado) {
      return;
    }
    this.pedidoApi.rechazar(this.pedidoSeleccionado.id, payload).subscribe({
      next: () => {
        this.toastService.success('Pedido rechazado.');
        this.cerrarModal();
        this.cargarPedidos();
      },
      error: () => this.toastService.error('No se pudo rechazar el pedido.')
    });
  }

  anularPedido(pedido: Pedido): void {
    const motivo = window.prompt('Motivo de la anulación:');
    if (!motivo) {
      return;
    }
    this.pedidoApi.anular(pedido.id, { motivo }).subscribe({
      next: () => {
        this.toastService.success('Pedido anulado.');
        this.cargarPedidos();
      },
      error: () => this.toastService.error('No se pudo anular el pedido.')
    });
  }
}
