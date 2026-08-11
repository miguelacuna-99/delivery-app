import { Component, computed, input, output } from '@angular/core';
import { ItemCarritoResponse } from '../../models/carrito.model';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { avatarClase, iniciales } from '../../../../shared/utils/avatar.util';

@Component({
  selector: 'app-carrito-item-row',
  imports: [ButtonComponent],
  templateUrl: './carrito-item-row.component.html',
  styleUrl: './carrito-item-row.component.scss'
})
export class CarritoItemRowComponent {
  readonly item = input.required<ItemCarritoResponse>();

  readonly cantidadChange = output<number>();
  readonly eliminar = output<void>();

  /** Tile de iniciales: el carrito no trae imagen de producto. */
  protected readonly iniciales = computed(() => iniciales(this.item().nombre ?? this.item().productoId));
  protected readonly avatarClase = computed(() => avatarClase(this.item().productoId));

  onDecrementar(): void {
    const nuevaCantidad = this.item().cantidad - 1;
    if (nuevaCantidad <= 0) {
      this.eliminar.emit();
      return;
    }
    this.cantidadChange.emit(nuevaCantidad);
  }

  onIncrementar(): void {
    this.cantidadChange.emit(this.item().cantidad + 1);
  }
}
