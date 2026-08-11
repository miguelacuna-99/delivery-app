import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ComercioApiService } from '../../services/comercio-api.service';
import { InputComponent } from '../../../../shared/components/input/input.component';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { ToastService } from '../../../../shared/services/toast.service';
import { AuthService } from '../../../../core/services/auth.service';
import { TipoUsuario } from '../../../../core/models/auth.model';

@Component({
  selector: 'app-mi-comercio-page',
  standalone: true,
  imports: [CommonModule, InputComponent, ButtonComponent, CardComponent, SpinnerComponent],
  template: `
    <h2 class="page-title">Mi comercio</h2>

    @if (loading()) {
      <div class="loading-state"><app-spinner></app-spinner></div>
    }

    @if (!loading()) {
      <app-card class="form-card">
        <app-input label="Nombre" [value]="nombre()" [disabled]="!canEdit()" (valueChange)="nombre.set($event)"></app-input>
        <app-input label="Dirección" [value]="direccion()" [disabled]="!canEdit()" (valueChange)="direccion.set($event)"></app-input>
        <app-input label="Teléfono" [value]="telefono()" [disabled]="!canEdit()" (valueChange)="telefono.set($event)"></app-input>
        <app-input label="Email" [value]="email()" [disabled]="!canEdit()" (valueChange)="email.set($event)"></app-input>
        @if (canEdit()) {
          <app-button [loading]="saving()" (clicked)="guardar()">Guardar cambios</app-button>
        }
      </app-card>
    }
  `,
  styleUrl: './mi-comercio-page.component.scss'
})
export class MiComercioPageComponent {
  private readonly comercioApi = inject(ComercioApiService);
  private readonly toastService = inject(ToastService);
  private readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly saving = signal(false);

  readonly nombre = signal('');
  readonly direccion = signal('');
  readonly telefono = signal('');
  readonly email = signal('');

  readonly canEdit = computed(() => {
    const tipo = this.authService.tipo();
    return tipo === TipoUsuario.ROOT || tipo === TipoUsuario.ADMIN;
  });

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.comercioApi.obtenerMiComercio().subscribe({
      next: (comercio) => {
        this.nombre.set(comercio.nombre ?? '');
        this.direccion.set(comercio.direccion ?? '');
        this.telefono.set(comercio.telefono ?? '');
        this.email.set(comercio.email ?? '');
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toastService.error('No se pudo cargar la información del comercio.');
      }
    });
  }

  guardar(): void {
    this.saving.set(true);
    this.comercioApi
      .actualizarMiComercio({
        nombre: this.nombre(),
        direccion: this.direccion(),
        telefono: this.telefono(),
        email: this.email()
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.toastService.success('Datos del comercio actualizados.');
        },
        error: () => {
          this.saving.set(false);
          this.toastService.error('No se pudieron guardar los cambios.');
        }
      });
  }
}
