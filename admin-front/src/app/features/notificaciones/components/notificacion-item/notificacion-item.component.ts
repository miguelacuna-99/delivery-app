import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Notificacion, TipoNotificacion } from '../../models/notificacion.model';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { IconComponent, IconName } from '../../../../shared/components/icon/icon.component';

const TIPO_LABEL: Record<TipoNotificacion, string> = {
  [TipoNotificacion.PEDIDO_PENDIENTE]: 'Pedido pendiente',
  [TipoNotificacion.PEDIDO_PAGADO]: 'Pedido pagado'
};

const TIPO_ICON: Record<TipoNotificacion, IconName> = {
  [TipoNotificacion.PEDIDO_PENDIENTE]: 'clock',
  [TipoNotificacion.PEDIDO_PAGADO]: 'check'
};

@Component({
  selector: 'app-notificacion-item',
  standalone: true,
  imports: [CommonModule, ButtonComponent, IconComponent],
  template: `
    <div class="notificacion-item">
      <span class="notificacion-item__dot" aria-hidden="true"></span>
      <span class="notificacion-item__icon">
        <app-icon [name]="tipoIcon" [size]="16"></app-icon>
      </span>
      <div class="notificacion-item__body">
        <span class="notificacion-item__tipo">{{ tipoLabel }}</span>
        <p class="notificacion-item__mensaje">{{ notificacion.mensaje }}</p>
        @if (notificacion.fechaCreacion) {
          <span class="notificacion-item__fecha">{{ notificacion.fechaCreacion | date: 'short' }}</span>
        }
      </div>
      <app-button variant="ghost" (clicked)="marcarLeida.emit()">Marcar leída</app-button>
    </div>
  `,
  styleUrl: './notificacion-item.component.scss'
})
export class NotificacionItemComponent {
  @Input({ required: true }) notificacion!: Notificacion;
  @Output() marcarLeida = new EventEmitter<void>();

  get tipoLabel(): string {
    return TIPO_LABEL[this.notificacion.tipo] ?? this.notificacion.tipo;
  }

  get tipoIcon(): IconName {
    return TIPO_ICON[this.notificacion.tipo] ?? 'notificaciones';
  }
}
