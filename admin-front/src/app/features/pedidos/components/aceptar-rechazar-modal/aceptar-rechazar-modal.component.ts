import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { InputComponent } from '../../../../shared/components/input/input.component';
import { IconComponent } from '../../../../shared/components/icon/icon.component';

export type AceptarRechazarAccion = 'aceptar' | 'rechazar';

export interface AceptarPayload {
  tiempoEstimadoMin: number;
}

export interface RechazarPayload {
  mensaje?: string;
}

@Component({
  selector: 'app-aceptar-rechazar-modal',
  standalone: true,
  imports: [CommonModule, ButtonComponent, InputComponent, IconComponent],
  template: `
    @if (visible) {
      <div class="modal-backdrop" (click)="cancelar.emit()">
        <div class="modal" (click)="$event.stopPropagation()">
          <div class="modal__header">
            <span class="modal__icon" [class.modal__icon--danger]="accion === 'rechazar'">
              <app-icon [name]="accion === 'aceptar' ? 'check' : 'x'" [size]="18"></app-icon>
            </span>
            <h3>{{ accion === 'aceptar' ? 'Aceptar pedido' : 'Rechazar pedido' }}</h3>
          </div>
          @if (accion === 'aceptar') {
            <app-input
              label="Tiempo estimado (minutos)"
              type="number"
              [value]="tiempoEstimadoMin()"
              (valueChange)="tiempoEstimadoMin.set($event)"
            ></app-input>
          } @else {
            <app-input
              label="Mensaje (opcional)"
              [value]="mensaje()"
              (valueChange)="mensaje.set($event)"
            ></app-input>
          }
          <div class="modal__actions">
            <app-button variant="ghost" (clicked)="cancelar.emit()">Cancelar</app-button>
            <app-button [variant]="accion === 'aceptar' ? 'primary' : 'danger'" (clicked)="confirmar()">Confirmar</app-button>
          </div>
        </div>
      </div>
    }
  `,
  styleUrl: './aceptar-rechazar-modal.component.scss'
})
export class AceptarRechazarModalComponent {
  @Input() visible = false;
  @Input() accion: AceptarRechazarAccion = 'aceptar';

  @Output() confirmarAceptar = new EventEmitter<AceptarPayload>();
  @Output() confirmarRechazar = new EventEmitter<RechazarPayload>();
  @Output() cancelar = new EventEmitter<void>();

  readonly tiempoEstimadoMin = signal<string>('15');
  readonly mensaje = signal<string>('');

  confirmar(): void {
    if (this.accion === 'aceptar') {
      const minutos = Number(this.tiempoEstimadoMin());
      this.confirmarAceptar.emit({ tiempoEstimadoMin: isNaN(minutos) ? 0 : minutos });
    } else {
      this.confirmarRechazar.emit({ mensaje: this.mensaje() || undefined });
    }
  }
}
