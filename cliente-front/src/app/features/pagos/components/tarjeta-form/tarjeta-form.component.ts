import { Component, computed, input, output, signal } from '@angular/core';
import { InputComponent } from '../../../../shared/components/input/input.component';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { TarjetaPreviewComponent } from '../tarjeta-preview/tarjeta-preview.component';
import { GuardarTarjetaRequest } from '../../models/tarjeta.model';
import { detectarMarca, formatearNumero, limpiarNumero, pasaLuhn } from '../../utils/tarjeta.util';

@Component({
  selector: 'app-tarjeta-form',
  imports: [InputComponent, ButtonComponent, TarjetaPreviewComponent],
  templateUrl: './tarjeta-form.component.html',
  styleUrl: './tarjeta-form.component.scss'
})
export class TarjetaFormComponent {
  readonly saving = input(false);
  // Cuando el panel de pago embebe este formulario, el "Pagar" es un unico
  // boton fuera de aqui: se oculta el propio boton de guardar.
  readonly mostrarBoton = input(true);

  readonly guardar = output<GuardarTarjetaRequest>();

  readonly numero = signal('');
  readonly titular = signal('');
  readonly vencimiento = signal('');
  // Nunca se envia al backend: no hace falta, no hay cobro real, es solo
  // parte del formulario para que se vea realista.
  readonly cvv = signal('');

  protected readonly numeroFormateado = computed(() => formatearNumero(this.numero()));
  protected readonly marca = computed(() => detectarMarca(this.numero()));

  readonly esValido = computed(() => {
    const [mesStr, anioStr] = this.vencimiento().split('/');
    const mes = Number(mesStr);
    const anio = Number(anioStr);
    if (!pasaLuhn(this.numero())) {
      return false;
    }
    if (!this.titular().trim()) {
      return false;
    }
    if (!mes || mes < 1 || mes > 12 || !anioStr || !anio) {
      return false;
    }
    return true;
  });

  onNumeroChange(valor: string): void {
    this.numero.set(limpiarNumero(valor).slice(0, 19));
  }

  onVencimientoChange(valor: string): void {
    this.vencimiento.set(valor.replace(/[^\d/]/g, '').slice(0, 5));
  }

  confirmar(): void {
    const datos = this.obtenerDatosValidos();
    if (datos) {
      this.guardar.emit(datos);
    }
  }

  /** Usado por app-pago-tarjeta-panel para leer el borrador cuando este formulario va embebido (mostrarBoton=false). */
  obtenerDatosValidos(): GuardarTarjetaRequest | null {
    if (!this.esValido()) {
      return null;
    }
    const [mesStr, anioStr] = this.vencimiento().split('/');
    const anio = anioStr.length === 2 ? 2000 + Number(anioStr) : Number(anioStr);
    return {
      numero: this.numero(),
      titular: this.titular().trim(),
      mesExpiracion: Number(mesStr),
      anioExpiracion: anio
    };
  }

  reset(): void {
    this.numero.set('');
    this.titular.set('');
    this.vencimiento.set('');
    this.cvv.set('');
  }
}
