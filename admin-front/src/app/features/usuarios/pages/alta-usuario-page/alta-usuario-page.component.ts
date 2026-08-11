import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CrearUsuarioRequest, UsuarioApiService } from '../../services/usuario-api.service';
import { InputComponent } from '../../../../shared/components/input/input.component';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { ToastService } from '../../../../shared/services/toast.service';
import { AuthService } from '../../../../core/services/auth.service';
import { TipoUsuario } from '../../../../core/models/auth.model';

@Component({
  selector: 'app-alta-usuario-page',
  standalone: true,
  imports: [CommonModule, InputComponent, ButtonComponent, CardComponent],
  template: `
    <h2 class="page-title">Alta de usuario</h2>

    <app-card class="form-card">
      <app-input label="Usuario" [value]="username()" (valueChange)="username.set($event)"></app-input>
      <app-input
        label="Contraseña"
        type="password"
        [value]="password()"
        (valueChange)="password.set($event)"
      ></app-input>
      <app-input label="Email" [value]="mail()" (valueChange)="mail.set($event)"></app-input>
      <app-input label="Teléfono" [value]="telefono()" (valueChange)="telefono.set($event)"></app-input>

      <div class="alta-usuario-page__tipo">
        <label for="tipo-select">Tipo</label>
        <select id="tipo-select" class="filter-select" [value]="tipo()" (change)="onTipoChange($event)">
          @for (opcion of tiposDisponibles(); track opcion) {
            <option [value]="opcion">{{ opcion }}</option>
          }
        </select>
      </div>

      <app-button [loading]="saving()" (clicked)="crear()">Crear usuario</app-button>
    </app-card>
  `,
  styleUrl: './alta-usuario-page.component.scss'
})
export class AltaUsuarioPageComponent {
  private readonly usuarioApi = inject(UsuarioApiService);
  private readonly toastService = inject(ToastService);
  private readonly authService = inject(AuthService);

  readonly username = signal('');
  readonly password = signal('');
  readonly mail = signal('');
  readonly telefono = signal('');
  readonly tipo = signal<TipoUsuario>(TipoUsuario.PERSONAL);
  readonly saving = signal(false);

  readonly tiposDisponibles = computed<TipoUsuario[]>(() => {
    const tipoActual = this.authService.tipo();
    if (tipoActual === TipoUsuario.ROOT) {
      return [TipoUsuario.ROOT, TipoUsuario.ADMIN, TipoUsuario.PERSONAL, TipoUsuario.REPARTIDOR];
    }
    return [TipoUsuario.PERSONAL, TipoUsuario.REPARTIDOR];
  });

  onTipoChange(event: Event): void {
    this.tipo.set((event.target as HTMLSelectElement).value as TipoUsuario);
  }

  crear(): void {
    if (!this.username() || !this.password()) {
      this.toastService.error('Introduce usuario y contraseña.');
      return;
    }
    const request: CrearUsuarioRequest = {
      username: this.username(),
      password: this.password(),
      mail: this.mail(),
      telefono: this.telefono(),
      tipo: this.tipo()
    };
    this.saving.set(true);
    this.usuarioApi.crear(request).subscribe({
      next: () => {
        this.saving.set(false);
        this.toastService.success('Usuario creado correctamente.');
        this.username.set('');
        this.password.set('');
        this.mail.set('');
        this.telefono.set('');
      },
      error: () => {
        this.saving.set(false);
        this.toastService.error('No se pudo crear el usuario.');
      }
    });
  }
}
