import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { TipoUsuario } from '../../core/models/auth.model';
import { IconComponent, IconName } from '../../shared/components/icon/icon.component';

interface MenuItem {
  label: string;
  path: string;
  icon: IconName;
  roles: TipoUsuario[];
}

const MENU_ITEMS: MenuItem[] = [
  { label: 'Dashboard', path: '/dashboard', icon: 'dashboard', roles: [TipoUsuario.ROOT, TipoUsuario.ADMIN, TipoUsuario.PERSONAL, TipoUsuario.REPARTIDOR] },
  { label: 'Pedidos', path: '/pedidos', icon: 'pedidos', roles: [TipoUsuario.ROOT, TipoUsuario.ADMIN, TipoUsuario.PERSONAL] },
  { label: 'Entregar pedido', path: '/pedidos/entregar', icon: 'entregar', roles: [TipoUsuario.REPARTIDOR] },
  { label: 'Notificaciones', path: '/notificaciones', icon: 'notificaciones', roles: [TipoUsuario.ROOT, TipoUsuario.ADMIN, TipoUsuario.PERSONAL] },
  { label: 'Mi comercio', path: '/comercio', icon: 'comercio', roles: [TipoUsuario.ROOT, TipoUsuario.ADMIN, TipoUsuario.PERSONAL, TipoUsuario.REPARTIDOR] },
  { label: 'Productos', path: '/productos', icon: 'productos', roles: [TipoUsuario.ROOT, TipoUsuario.ADMIN] },
  { label: 'Cupones', path: '/cupones', icon: 'cupones', roles: [TipoUsuario.ROOT, TipoUsuario.ADMIN] },
  { label: 'Usuarios', path: '/usuarios', icon: 'usuarios', roles: [TipoUsuario.ROOT, TipoUsuario.ADMIN] }
];

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, IconComponent],
  template: `
    <nav class="app-sidebar">
      @for (item of visibleItems(); track item.path) {
        <a class="app-sidebar__link" [routerLink]="item.path" routerLinkActive="app-sidebar__link--active">
          <app-icon [name]="item.icon" [size]="18"></app-icon>
          <span class="app-sidebar__label">{{ item.label }}</span>
        </a>
      }
    </nav>
  `,
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  private readonly authService = inject(AuthService);

  readonly visibleItems = computed(() => {
    const tipo = this.authService.tipo();
    if (!tipo) {
      return [];
    }
    return MENU_ITEMS.filter((item) => item.roles.includes(tipo));
  });
}
