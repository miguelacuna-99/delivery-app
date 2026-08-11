import { Component, computed, input, output } from '@angular/core';
import { Producto } from '../../models/producto.model';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { avatarClase, iniciales } from '../../../../shared/utils/avatar.util';

@Component({
  selector: 'app-producto-card',
  imports: [CardComponent, ButtonComponent],
  templateUrl: './producto-card.component.html',
  styleUrl: './producto-card.component.scss'
})
export class ProductoCardComponent {
  readonly producto = input.required<Producto>();
  readonly añadirAlCarrito = output<Producto>();

  /** Tile de iniciales: el backend no expone imagen de producto. */
  protected readonly iniciales = computed(() => iniciales(this.producto().nombre));
  protected readonly avatarClase = computed(() => avatarClase(this.producto().id));

  onAnadir(): void {
    this.añadirAlCarrito.emit(this.producto());
  }
}
