import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { InputComponent } from '../../../../shared/components/input/input.component';
import { ActualizarProductoRequest, Producto } from '../../services/producto-api.service';

@Component({
  selector: 'app-editar-producto-modal',
  standalone: true,
  imports: [CommonModule, ButtonComponent, InputComponent],
  template: `
    @if (visible) {
      <div class="modal-backdrop" (click)="cancelar.emit()">
        <div class="modal" (click)="$event.stopPropagation()">
          <div class="modal__header">
            <h3>Editar producto</h3>
          </div>
          <app-input label="Nombre" [value]="nombre()" (valueChange)="nombre.set($event)"></app-input>
          <app-input label="Descripción" [value]="descripcion()" (valueChange)="descripcion.set($event)"></app-input>
          <app-input label="Ingredientes" [value]="ingredientes()" (valueChange)="ingredientes.set($event)"></app-input>
          <app-input label="URL de imagen" [value]="imagenUrl()" (valueChange)="imagenUrl.set($event)"></app-input>
          <app-input label="Precio" type="number" [value]="precio()" (valueChange)="precio.set($event)"></app-input>
          <div class="modal__actions">
            <app-button variant="ghost" (clicked)="cancelar.emit()">Cancelar</app-button>
            <app-button [loading]="saving" (clicked)="confirmar()">Guardar cambios</app-button>
          </div>
        </div>
      </div>
    }
  `,
  styleUrl: './editar-producto-modal.component.scss'
})
export class EditarProductoModalComponent {
  @Input() visible = false;
  @Input() saving = false;
  @Input() set producto(producto: Producto | null) {
    if (!producto) {
      return;
    }
    this.nombre.set(producto.nombre ?? '');
    this.descripcion.set(producto.descripcion ?? '');
    this.ingredientes.set(producto.ingredientes ?? '');
    this.imagenUrl.set(producto.imagenUrl ?? '');
    this.precio.set(String(producto.precio ?? ''));
  }

  @Output() guardar = new EventEmitter<ActualizarProductoRequest>();
  @Output() cancelar = new EventEmitter<void>();

  readonly nombre = signal('');
  readonly descripcion = signal('');
  readonly ingredientes = signal('');
  readonly imagenUrl = signal('');
  readonly precio = signal('');

  confirmar(): void {
    this.guardar.emit({
      nombre: this.nombre(),
      descripcion: this.descripcion(),
      ingredientes: this.ingredientes() || undefined,
      imagenUrl: this.imagenUrl() || undefined,
      precio: Number(this.precio()) || 0
    });
  }
}
