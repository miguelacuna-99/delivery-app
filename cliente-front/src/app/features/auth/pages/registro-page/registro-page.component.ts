import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { toAppHttpError } from '../../../../core/models/http-error.model';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { InputComponent } from '../../../../shared/components/input/input.component';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-registro-page',
  imports: [FormsModule, RouterLink, ButtonComponent, InputComponent, CardComponent],
  templateUrl: './registro-page.component.html',
  styleUrl: './registro-page.component.scss'
})
export class RegistroPageComponent {
  private readonly authApi = inject(AuthApiService);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);

  readonly username = signal('');
  readonly password = signal('');
  readonly mail = signal('');
  readonly direccionDomicilio = signal('');
  readonly telefono = signal('');

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  submit(): void {
    if (
      !this.username() ||
      !this.password() ||
      !this.mail() ||
      !this.direccionDomicilio() ||
      !this.telefono()
    ) {
      this.errorMessage.set('Rellena todos los campos.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authApi
      .registro({
        username: this.username(),
        password: this.password(),
        mail: this.mail(),
        direccionDomicilio: this.direccionDomicilio(),
        telefono: this.telefono()
      })
      .subscribe({
        next: () => {
          this.loading.set(false);
          this.toastService.show('Cuenta creada. Ya puedes iniciar sesion.', 'success');
          void this.router.navigateByUrl('/login');
        },
        error: (err) => {
          this.loading.set(false);
          this.errorMessage.set(toAppHttpError(err).message);
        }
      });
  }
}
