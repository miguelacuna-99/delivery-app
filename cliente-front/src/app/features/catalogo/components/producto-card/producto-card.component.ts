import { Component, computed, input, output, signal } from '@angular/core';
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

  private readonly imagenConError = signal(false);

  /** Tile de iniciales: fallback cuando el producto no tiene imagen o la URL no carga. */
  protected readonly mostrarImagen = computed(() => !!this.producto().imagenUrl && !this.imagenConError());
  protected readonly iniciales = computed(() => iniciales(this.producto().nombre));
  protected readonly avatarClase = computed(() => avatarClase(this.producto().id));

  onAnadir(): void {
    this.añadirAlCarrito.emit(this.producto());
  }

  onImagenError(): void {
    this.imagenConError.set(true);
  }
}
