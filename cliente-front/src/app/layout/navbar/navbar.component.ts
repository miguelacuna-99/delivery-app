import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { PuntosApiService } from '../../features/puntos/services/puntos-api.service';
import { ButtonComponent } from '../../shared/components/button/button.component';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive, ButtonComponent],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly puntosApi = inject(PuntosApiService);
  private readonly router = inject(Router);

  readonly currentUser = this.authService.currentUser;
  readonly saldoPuntos = signal<number | null>(null);

  ngOnInit(): void {
    this.puntosApi.misPuntos().subscribe({
      next: (res) => this.saldoPuntos.set(res.saldo),
      error: () => this.saldoPuntos.set(null)
    });
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}
