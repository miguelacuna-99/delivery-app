import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { InputComponent } from '../../../../shared/components/input/input.component';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonComponent, InputComponent, CardComponent],
  template: `
    <div class="login-page">
      <div class="login-page__brand">
        <span class="login-page__mark" aria-hidden="true"></span>
        <span>DeliveryApp <strong>Admin</strong></span>
      </div>
      <app-card title="Iniciar sesión" class="login-page__card">
        @if (expiredMessage()) {
          <p class="login-page__expired">Tu sesión ha expirado. Vuelve a iniciar sesión.</p>
        }
        <form (ngSubmit)="submit()">
          <app-input label="Usuario" [value]="username()" (valueChange)="username.set($event)"></app-input>
          <app-input
            label="Contraseña"
            type="password"
            [value]="password()"
            (valueChange)="password.set($event)"
          ></app-input>
          @if (errorMessage()) {
            <p class="login-page__error">{{ errorMessage() }}</p>
          }
          <app-button type="submit" [loading]="loading()">Entrar</app-button>
        </form>
      </app-card>
    </div>
  `,
  styleUrl: './login-page.component.scss'
})
export class LoginPageComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toastService = inject(ToastService);

  readonly username = signal('');
  readonly password = signal('');
  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly expiredMessage = signal(false);

  constructor() {
    this.expiredMessage.set(this.route.snapshot.queryParamMap.get('expired') === '1');
  }

  async submit(): Promise<void> {
    if (!this.username() || !this.password()) {
      this.errorMessage.set('Introduce usuario y contraseña.');
      return;
    }
    this.loading.set(true);
    this.errorMessage.set('');
    try {
      await this.authService.login({ username: this.username(), password: this.password() });
      this.toastService.success('Sesión iniciada correctamente.');
      this.router.navigate(['/dashboard']);
    } catch {
      this.errorMessage.set('Usuario o contraseña incorrectos.');
    } finally {
      this.loading.set(false);
    }
  }
}
