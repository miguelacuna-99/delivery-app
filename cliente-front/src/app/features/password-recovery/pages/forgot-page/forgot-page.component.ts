import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PasswordApiService } from '../../services/password-api.service';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { InputComponent } from '../../../../shared/components/input/input.component';
import { CardComponent } from '../../../../shared/components/card/card.component';

/** Andamiaje: el backend siempre responde 204 para no revelar si el mail existe. */
@Component({
  selector: 'app-forgot-page',
  imports: [FormsModule, RouterLink, ButtonComponent, InputComponent, CardComponent],
  templateUrl: './forgot-page.component.html',
  styleUrl: './forgot-page.component.scss'
})
export class ForgotPageComponent {
  private readonly passwordApi = inject(PasswordApiService);

  readonly mail = signal('');
  readonly loading = signal(false);
  readonly sent = signal(false);

  submit(): void {
    if (!this.mail()) {
      return;
    }
    this.loading.set(true);
    this.passwordApi.forgot(this.mail()).subscribe({
      next: () => {
        this.loading.set(false);
        this.sent.set(true);
      },
      error: () => {
        this.loading.set(false);
        this.sent.set(true);
      }
    });
  }
}
