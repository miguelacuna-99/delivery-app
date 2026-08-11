import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { ToastComponent } from '../../shared/components/toast/toast.component';

@Component({
  selector: 'app-authenticated-shell',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, SidebarComponent, ToastComponent],
  template: `
    <div class="app-shell">
      <app-navbar></app-navbar>
      <div class="app-shell__body">
        <app-sidebar></app-sidebar>
        <main class="app-shell__content">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
    <app-toast></app-toast>
  `,
  styleUrl: './authenticated-shell.component.scss'
})
export class AuthenticatedShellComponent {}
