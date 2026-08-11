import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { IconComponent, IconName } from '../../../../shared/components/icon/icon.component';
import { TipoUsuario } from '../../../../core/models/auth.model';

interface DashboardTile {
  label: string;
  description: string;
  path: string;
  icon: IconName;
  roles: TipoUsuario[];
}

const TILES: DashboardTile[] = [
  {
    label: 'Bandeja de pedidos',
    description: 'Consulta y gestiona los pedidos del comercio.',
    path: '/pedidos',
    icon: 'pedidos',
    roles: [TipoUsuario.ROOT, TipoUsuario.ADMIN, TipoUsuario.PERSONAL]
  },
  {
    label: 'Entregar pedido',
    description: 'Marca un pedido como entregado por su número.',
    path: '/pedidos/entregar',
    icon: 'entregar',
    roles: [TipoUsuario.REPARTIDOR]
  },
  {
    label: 'Notificaciones',
    description: 'Revisa avisos de pedidos pendientes y pagados.',
    path: '/notificaciones',
    icon: 'notificaciones',
    roles: [TipoUsuario.ROOT, TipoUsuario.ADMIN, TipoUsuario.PERSONAL]
  },
  {
    label: 'Mi comercio',
    description: 'Consulta y edita los datos del comercio.',
    path: '/comercio',
    icon: 'comercio',
    roles: [TipoUsuario.ROOT, TipoUsuario.ADMIN, TipoUsuario.PERSONAL, TipoUsuario.REPARTIDOR]
  },
  {
    label: 'Productos',
    description: 'Gestiona el catálogo de productos.',
    path: '/productos',
    icon: 'productos',
    roles: [TipoUsuario.ROOT, TipoUsuario.ADMIN]
  },
  {
    label: 'Cupones',
    description: 'Consulta y gestiona los cupones del comercio.',
    path: '/cupones',
    icon: 'cupones',
    roles: [TipoUsuario.ROOT, TipoUsuario.ADMIN]
  },
  {
    label: 'Usuarios',
    description: 'Da de alta nuevos usuarios del comercio.',
    path: '/usuarios',
    icon: 'usuarios',
    roles: [TipoUsuario.ROOT, TipoUsuario.ADMIN]
  }
];

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, RouterLink, CardComponent, IconComponent],
  template: `
    <h2 class="page-title">Bienvenido, {{ authService.username() }}</h2>
    <div class="dashboard-grid">
      @for (tile of visibleTiles(); track tile.path) {
        <a [routerLink]="tile.path" class="dashboard-grid__link">
          <app-card class="dashboard-grid__card">
            <span class="dashboard-grid__icon">
              <app-icon [name]="tile.icon" [size]="20"></app-icon>
            </span>
            <h3 class="dashboard-grid__title">{{ tile.label }}</h3>
            <p>{{ tile.description }}</p>
          </app-card>
        </a>
      }
    </div>
  `,
  styleUrl: './dashboard-page.component.scss'
})
export class DashboardPageComponent {
  readonly authService = inject(AuthService);

  readonly visibleTiles = computed(() => {
    const tipo = this.authService.tipo();
    if (!tipo) {
      return [];
    }
    return TILES.filter((tile) => tile.roles.includes(tipo));
  });
}
