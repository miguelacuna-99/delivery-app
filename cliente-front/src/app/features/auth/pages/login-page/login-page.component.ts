import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { TipoUsuario } from '../../../../core/models/auth.model';
import { toAppHttpError } from '../../../../core/models/http-error.model';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { InputComponent } from '../../../../shared/components/input/input.component';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-login-page',
  imports: [FormsModule, RouterLink, ButtonComponent, InputComponent, CardComponent],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss'
})
export class LoginPageComponent {
  private readonly authApi = inject(AuthApiService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toastService = inject(ToastService);

  readonly username = signal('');
  readonly password = signal('');
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly expired = this.route.snapshot.queryParamMap.get('expired') === '1';

  submit(): void {
    if (!this.username() || !this.password()) {
      this.errorMessage.set('Introduce usuario y contraseña.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authApi.login({ username: this.username(), password: this.password() }).subscribe({
      next: (response) => {
        this.authService.login(response);
        this.loading.set(false);

        if (response.mustChangePassword) {
          this.toastService.show('Debes cambiar tu contraseña.', 'warning');
        }

        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        if (returnUrl) {
          void this.router.navigateByUrl(returnUrl);
          return;
        }
        // Cada cliente esta atado a un unico comercio desde su registro: se le
        // lleva directo a su catalogo, nunca al listado multi-comercio.
        const destino =
          response.tipo === TipoUsuario.CLIENTE && this.authService.comercioId()
            ? `/catalogo/${this.authService.comercioId()}`
            : '/catalogo';
        void this.router.navigateByUrl(destino);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(toAppHttpError(err).message);
      }
    });
  }
}
