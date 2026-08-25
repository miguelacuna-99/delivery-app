import { DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { Pedido } from '../../models/pedido.model';
import { PedidoTimelineComponent } from '../pedido-timeline/pedido-timeline.component';
import { PagoTarjetaPanelComponent } from '../pago-tarjeta-panel/pago-tarjeta-panel.component';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { BadgeEstadoPedidoComponent } from '../../../../shared/components/badge-estado-pedido/badge-estado-pedido.component';

@Component({
  selector: 'app-pedido-card',
  imports: [DatePipe, PedidoTimelineComponent, PagoTarjetaPanelComponent, CardComponent, BadgeEstadoPedidoComponent],
  templateUrl: './pedido-card.component.html',
  styleUrl: './pedido-card.component.scss'
})
export class PedidoCardComponent {
  readonly pedido = input.required<Pedido>();
  readonly destacado = input(false);
}
