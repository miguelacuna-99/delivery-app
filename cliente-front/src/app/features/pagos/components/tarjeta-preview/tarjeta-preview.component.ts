import { Component, computed, input } from '@angular/core';
import { MarcaTarjeta } from '../../models/tarjeta.model';

@Component({
  selector: 'app-tarjeta-preview',
  templateUrl: './tarjeta-preview.component.html',
  styleUrl: './tarjeta-preview.component.scss'
})
export class TarjetaPreviewComponent {
  readonly numero = input('');
  readonly titular = input('');
  readonly vencimiento = input('');
  readonly marca = input<MarcaTarjeta>('OTRA');

  protected readonly numeroMostrado = computed(() => this.numero() || '•••• •••• •••• ••••');
  protected readonly claseMarca = computed(() => `tarjeta-preview--${this.marca().toLowerCase()}`);
}
