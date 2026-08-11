import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { ButtonComponent } from '../../shared/components/button/button.component';
import { IconComponent } from '../../shared/components/icon/icon.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, ButtonComponent, IconComponent],
  template: `
    <header class="app-navbar">
      <span class="app-navbar__brand">
        <span class="app-navbar__mark" aria-hidden="true"></span>
        DeliveryApp <span class="app-navbar__brand-accent">Admin</span>
      </span>
      <div class="app-navbar__user">
        @if (authService.currentUser(); as user) {
          <span class="app-navbar__username">{{ user.username }}</span>
          <span class="app-navbar__tipo">{{ user.tipo }}</span>
        }
        <app-button variant="ghost" (clicked)="logout()">
          <app-icon name="logout" [size]="16"></app-icon>
          Cerrar sesión
        </app-button>
      </div>
    </header>
  `,
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {
  readonly authService = inject(AuthService);

  logout(): void {
    this.authService.logoutAndRedirect(false);
  }
}
