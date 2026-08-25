import { Component, ViewChild, inject, input, signal } from '@angular/core';
import { TarjetaApiService } from '../../../pagos/services/tarjeta-api.service';
import { Tarjeta } from '../../../pagos/models/tarjeta.model';
import { TarjetaFormComponent } from '../../../pagos/components/tarjeta-form/tarjeta-form.component';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { ToastService } from '../../../../shared/services/toast.service';
import { toAppHttpError } from '../../../../core/models/http-error.model';
import { PedidoApiService } from '../../services/pedido-api.service';

@Component({
  selector: 'app-pago-tarjeta-panel',
  imports: [TarjetaFormComponent, ButtonComponent, SpinnerComponent],
  templateUrl: './pago-tarjeta-panel.component.html',
  styleUrl: './pago-tarjeta-panel.component.scss'
})
export class PagoTarjetaPanelComponent {
  @ViewChild(TarjetaFormComponent) private readonly form?: TarjetaFormComponent;

  readonly pedidoId = input.required<string>();

  private readonly tarjetaApi = inject(TarjetaApiService);
  private readonly pedidoApi = inject(PedidoApiService);
  private readonly toastService = inject(ToastService);

  readonly abierto = signal(false);
  readonly cargandoTarjetas = signal(false);
  readonly tarjetas = signal<Tarjeta[]>([]);
  readonly tarjetaSeleccionada = signal<string | null>(null);
  readonly mostrarFormNueva = signal(false);
  readonly pagando = signal(false);
  readonly pagado = signal(false);

  abrir(): void {
    this.abierto.set(true);
    if (this.tarjetas().length === 0) {
      this.cargarTarjetas();
    }
  }

  private cargarTarjetas(): void {
    this.cargandoTarjetas.set(true);
    this.tarjetaApi.listar().subscribe({
      next: (tarjetas) => {
        this.tarjetas.set(tarjetas);
        this.cargandoTarjetas.set(false);
        if (tarjetas.length === 0) {
          this.mostrarFormNueva.set(true);
        } else {
          this.tarjetaSeleccionada.set(tarjetas[0].id);
        }
      },
      error: (err) => {
        this.cargandoTarjetas.set(false);
        this.toastService.show(toAppHttpError(err).message, 'error');
      }
    });
  }

  seleccionar(id: string): void {
    this.tarjetaSeleccionada.set(id);
    this.mostrarFormNueva.set(false);
  }

  anadirNueva(): void {
    this.tarjetaSeleccionada.set(null);
    this.mostrarFormNueva.set(true);
  }

  puedePagar(): boolean {
    if (this.pagando()) {
      return false;
    }
    if (this.mostrarFormNueva()) {
      return !!this.form?.esValido();
    }
    return !!this.tarjetaSeleccionada();
  }

  pagar(): void {
    if (this.mostrarFormNueva()) {
      const datos = this.form?.obtenerDatosValidos();
      if (!datos) {
        return;
      }
      this.pagando.set(true);
      this.tarjetaApi.guardar(datos).subscribe({
        next: (tarjeta) => this.pagarConTarjeta(tarjeta.id),
        error: (err) => {
          this.pagando.set(false);
          this.toastService.show(toAppHttpError(err).message, 'error');
        }
      });
      return;
    }
    const id = this.tarjetaSeleccionada();
    if (!id) {
      return;
    }
    this.pagando.set(true);
    this.pagarConTarjeta(id);
  }

  private pagarConTarjeta(tarjetaId: string): void {
    this.pedidoApi.pagar(this.pedidoId(), tarjetaId).subscribe({
      next: () => {
        this.pagando.set(false);
        this.pagado.set(true);
        this.abierto.set(false);
        this.toastService.show('Pago enviado, te avisaremos cuando se confirme.', 'success');
      },
      error: (err) => {
        this.pagando.set(false);
        this.toastService.show(toAppHttpError(err).message, 'error');
      }
    });
  }
}
