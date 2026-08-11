import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PasswordApiService } from '../../services/password-api.service';
import { toAppHttpError } from '../../../../core/models/http-error.model';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { InputComponent } from '../../../../shared/components/input/input.component';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-reset-page',
  imports: [FormsModule, RouterLink, ButtonComponent, InputComponent, CardComponent],
  templateUrl: './reset-page.component.html',
  styleUrl: './reset-page.component.scss'
})
export class ResetPageComponent {
  private readonly passwordApi = inject(PasswordApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);

  readonly token = this.route.snapshot.queryParamMap.get('token') ?? '';
  readonly nuevaPassword = signal('');
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  submit(): void {
    if (!this.token) {
      this.errorMessage.set('El enlace no es valido: falta el token.');
      return;
    }
    if (!this.nuevaPassword()) {
      this.errorMessage.set('Introduce la nueva contraseña.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.passwordApi.reset(this.token, this.nuevaPassword()).subscribe({
      next: () => {
        this.loading.set(false);
        this.toastService.show('Contraseña actualizada. Ya puedes iniciar sesion.', 'success');
        void this.router.navigateByUrl('/login');
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(toAppHttpError(err).message);
      }
    });
  }
}
