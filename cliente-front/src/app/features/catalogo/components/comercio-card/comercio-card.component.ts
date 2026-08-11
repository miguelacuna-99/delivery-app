import { Component, computed, input, output } from '@angular/core';
import { Comercio } from '../../models/comercio.model';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { avatarClase, iniciales } from '../../../../shared/utils/avatar.util';

@Component({
  selector: 'app-comercio-card',
  imports: [CardComponent],
  templateUrl: './comercio-card.component.html',
  styleUrl: './comercio-card.component.scss'
})
export class ComercioCardComponent {
  readonly comercio = input.required<Comercio>();
  readonly seleccionar = output<Comercio>();

  /** Tile de iniciales: el backend no expone imagen de comercio. */
  protected readonly iniciales = computed(() => iniciales(this.comercio().nombre));
  protected readonly avatarClase = computed(() => avatarClase(this.comercio().id));

  onSeleccionar(): void {
    this.seleccionar.emit(this.comercio());
  }
}
