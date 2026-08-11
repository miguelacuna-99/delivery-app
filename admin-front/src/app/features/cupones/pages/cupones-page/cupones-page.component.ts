import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CrearCuponRequest, Cupon, CuponApiService } from '../../services/cupon-api.service';
import { InputComponent } from '../../../../shared/components/input/input.component';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { IconComponent } from '../../../../shared/components/icon/icon.component';
import { ToastService } from '../../../../shared/services/toast.service';
import { AuthService } from '../../../../core/services/auth.service';
import { TipoUsuario } from '../../../../core/models/auth.model';

@Component({
  selector: 'app-cupones-page',
  standalone: true,
  imports: [CommonModule, InputComponent, ButtonComponent, CardComponent, SpinnerComponent, IconComponent],
  template: `
    <h2 class="page-title">Cupones</h2>

    @if (canEscribir()) {
      <app-card title="Nuevo cupón" class="form-card cupones-page__form">
        <app-input label="Código" [value]="codigo()" (valueChange)="codigo.set($event)"></app-input>
        <app-input
          label="% descuento"
          type="number"
          [value]="porcentajeDescuento()"
          (valueChange)="porcentajeDescuento.set($event)"
        ></app-input>
        <app-input
          label="Fecha de caducidad"
          type="date"
          [value]="fechaCaducidad()"
          (valueChange)="fechaCaducidad.set($event)"
        ></app-input>
        <app-button [loading]="saving()" (clicked)="crear()">Crear cupón</app-button>
      </app-card>
    }

    @if (loading()) {
      <div class="loading-state"><app-spinner></app-spinner></div>
    }

    @if (!loading() && cupones().length === 0) {
      <div class="empty-state">
        <app-icon name="cupones" [size]="32"></app-icon>
        <p>No hay cupones todavía.</p>
      </div>
    }

    @if (cupones().length > 0) {
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>Código</th>
              <th>% descuento</th>
              <th>Caducidad</th>
              <th>Estado</th>
              @if (canEscribir()) {
                <th>Acciones</th>
              }
            </tr>
          </thead>
          <tbody>
            @for (cupon of cupones(); track cupon.id) {
              <tr>
                <td>{{ cupon.codigo }}</td>
                <td>{{ cupon.porcentajeDescuento }}%</td>
                <td>{{ cupon.fechaCaducidad | date: 'shortDate' }}</td>
                <td>
                  <span class="status-pill" [class]="estadoPillClass(cupon.estado)">{{ cupon.estado }}</span>
                </td>
                @if (canEscribir()) {
                  <td>
                    @if (cupon.estado === 'ACTIVO') {
                      <app-button variant="danger" (clicked)="anular(cupon)">Anular</app-button>
                    }
                  </td>
                }
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
  styleUrl: './cupones-page.component.scss'
})
export class CuponesPageComponent {
  private readonly cuponApi = inject(CuponApiService);
  private readonly toastService = inject(ToastService);
  private readonly authService = inject(AuthService);

  readonly cupones = signal<Cupon[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);

  readonly codigo = signal('');
  readonly porcentajeDescuento = signal('');
  readonly fechaCaducidad = signal('');

  readonly canEscribir = computed(() => this.authService.tipo() === TipoUsuario.ADMIN);

  constructor() {
    this.cargar();
  }

  estadoPillClass(estado: Cupon['estado']): string {
    if (estado === 'ACTIVO') {
      return 'status-pill--success';
    }
    if (estado === 'ANULADO') {
      return 'status-pill--danger';
    }
    return 'status-pill--neutral';
  }

  cargar(): void {
    this.loading.set(true);
    this.cuponApi.listar().subscribe({
      next: (cupones) => {
        this.cupones.set(cupones);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toastService.error('No se pudieron cargar los cupones.');
      }
    });
  }

  crear(): void {
    const request: CrearCuponRequest = {
      codigo: this.codigo(),
      porcentajeDescuento: Number(this.porcentajeDescuento()) || 0,
      fechaCaducidad: this.fechaCaducidad()
    };
    if (!request.codigo || !request.fechaCaducidad) {
      this.toastService.error('Introduce código y fecha de caducidad.');
      return;
    }
    this.saving.set(true);
    this.cuponApi.crear(request).subscribe({
      next: () => {
        this.saving.set(false);
        this.codigo.set('');
        this.porcentajeDescuento.set('');
        this.fechaCaducidad.set('');
        this.toastService.success('Cupón creado.');
        this.cargar();
      },
      error: () => {
        this.saving.set(false);
        this.toastService.error('No se pudo crear el cupón.');
      }
    });
  }

  anular(cupon: Cupon): void {
    this.cuponApi.anular(cupon.id).subscribe({
      next: () => {
        this.toastService.success('Cupón anulado.');
        this.cargar();
      },
      error: () => this.toastService.error('No se pudo anular el cupón.')
    });
  }
}
