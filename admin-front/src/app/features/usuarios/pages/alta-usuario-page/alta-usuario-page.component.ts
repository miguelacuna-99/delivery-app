import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CrearUsuarioRequest, Usuario, UsuarioApiService } from '../../services/usuario-api.service';
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
    <div class="page-stack">
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

      @if (esRoot()) {
        <h2 class="page-title">Usuarios del comercio</h2>

        @if (loadingUsuarios()) {
          <p>Cargando usuarios...</p>
        } @else if (usuarios().length === 0) {
          <div class="empty-state">
            <p>No hay usuarios todavía.</p>
          </div>
        } @else {
          <div class="table-wrapper">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Email</th>
                  <th>Teléfono</th>
                  <th>Tipo</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                @for (usuario of usuarios(); track usuario.id) {
                  <tr>
                    <td>{{ usuario.username }}</td>
                    <td>{{ usuario.mail }}</td>
                    <td>{{ usuario.telefono }}</td>
                    <td>{{ usuario.tipo }}</td>
                    <td>
                      <app-button variant="danger" (clicked)="eliminar(usuario)">Eliminar</app-button>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      }
    </div>
  `,
  styleUrl: './alta-usuario-page.component.scss'
})
export class AltaUsuarioPageComponent implements OnInit {
  private readonly usuarioApi = inject(UsuarioApiService);
  private readonly toastService = inject(ToastService);
  private readonly authService = inject(AuthService);

  readonly username = signal('');
  readonly password = signal('');
  readonly mail = signal('');
  readonly telefono = signal('');
  readonly tipo = signal<TipoUsuario>(TipoUsuario.PERSONAL);
  readonly saving = signal(false);

  readonly esRoot = computed(() => this.authService.tipo() === TipoUsuario.ROOT);
  readonly usuarios = signal<Usuario[]>([]);
  readonly loadingUsuarios = signal(false);

  readonly tiposDisponibles = computed<TipoUsuario[]>(() => {
    const tipoActual = this.authService.tipo();
    if (tipoActual === TipoUsuario.ROOT) {
      return [TipoUsuario.ROOT, TipoUsuario.ADMIN, TipoUsuario.PERSONAL, TipoUsuario.REPARTIDOR];
    }
    return [TipoUsuario.PERSONAL, TipoUsuario.REPARTIDOR];
  });

  ngOnInit(): void {
    if (this.esRoot()) {
      this.cargarUsuarios();
    }
  }

  onTipoChange(event: Event): void {
    this.tipo.set((event.target as HTMLSelectElement).value as TipoUsuario);
  }

  cargarUsuarios(): void {
    this.loadingUsuarios.set(true);
    this.usuarioApi.listar().subscribe({
      next: (usuarios) => {
        this.usuarios.set(usuarios);
        this.loadingUsuarios.set(false);
      },
      error: () => {
        this.loadingUsuarios.set(false);
        this.toastService.error('No se pudieron cargar los usuarios.');
      }
    });
  }

  eliminar(usuario: Usuario): void {
    if (!confirm(`¿Eliminar al usuario "${usuario.username}"?`)) {
      return;
    }
    this.usuarioApi.eliminar(usuario.id).subscribe({
      next: () => {
        this.toastService.success('Usuario eliminado.');
        this.cargarUsuarios();
      },
      error: () => this.toastService.error('No se pudo eliminar el usuario.')
    });
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
        if (this.esRoot()) {
          this.cargarUsuarios();
        }
      },
      error: () => {
        this.saving.set(false);
        this.toastService.error('No se pudo crear el usuario.');
      }
    });
  }
}
