import { Component, DestroyRef, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificacionApiService } from '../../services/notificacion-api.service';
import { Notificacion, TipoNotificacion } from '../../models/notificacion.model';
import { NotificacionItemComponent } from '../../components/notificacion-item/notificacion-item.component';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { IconComponent } from '../../../../shared/components/icon/icon.component';
import { ToastService } from '../../../../shared/services/toast.service';
import { NOTIFICACIONES_POLLING_MS } from '../../../../shared/constants/polling.constants';

@Component({
  selector: 'app-notificaciones-page',
  standalone: true,
  imports: [CommonModule, NotificacionItemComponent, SpinnerComponent, IconComponent],
  template: `
    <h2 class="page-title">Notificaciones</h2>

    <div class="filter-bar">
      <label for="tipo-select">Tipo</label>
      <select id="tipo-select" class="filter-select" [value]="tipoSeleccionado()" (change)="onTipoChange($event)">
        @for (tipo of tipos; track tipo) {
          <option [value]="tipo">{{ tipo }}</option>
        }
      </select>
      @if (!loading() && notificaciones().length > 0) {
        <span class="notificaciones-page__count">{{ notificaciones().length }} sin leer</span>
      }
    </div>

    @if (loading()) {
      <div class="loading-state"><app-spinner></app-spinner></div>
    }

    @if (!loading() && notificaciones().length === 0) {
      <div class="empty-state">
        <app-icon name="notificaciones" [size]="32"></app-icon>
        <p>No hay notificaciones pendientes.</p>
      </div>
    }

    <div class="notificaciones-page__list">
      @for (notificacion of notificaciones(); track notificacion.id) {
        <app-notificacion-item
          [notificacion]="notificacion"
          (marcarLeida)="marcarLeida(notificacion)"
        ></app-notificacion-item>
      }
    </div>
  `,
  styleUrl: './notificaciones-page.component.scss'
})
export class NotificacionesPageComponent {
  private readonly notificacionApi = inject(NotificacionApiService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  readonly tipos = Object.values(TipoNotificacion);

  readonly notificaciones = signal<Notificacion[]>([]);
  readonly loading = signal(false);
  readonly tipoSeleccionado = signal<TipoNotificacion>(TipoNotificacion.PEDIDO_PENDIENTE);

  constructor() {
    this.cargar();
    const intervalId = setInterval(() => this.cargar(), NOTIFICACIONES_POLLING_MS);
    this.destroyRef.onDestroy(() => clearInterval(intervalId));
  }

  onTipoChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as TipoNotificacion;
    this.tipoSeleccionado.set(value);
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.notificacionApi.listar(this.tipoSeleccionado()).subscribe({
      next: (notificaciones) => {
        this.notificaciones.set(notificaciones);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toastService.error('No se pudieron cargar las notificaciones.');
      }
    });
  }

  marcarLeida(notificacion: Notificacion): void {
    this.notificacionApi.marcarLeida(notificacion.id).subscribe({
      next: () => {
        this.notificaciones.update((items) => items.filter((n) => n.id !== notificacion.id));
      },
      error: () => this.toastService.error('No se pudo marcar la notificación como leída.')
    });
  }
}
