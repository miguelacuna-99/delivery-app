import { Component, OnInit, ViewChild, inject, signal } from '@angular/core';
import { TarjetaApiService } from '../../services/tarjeta-api.service';
import { GuardarTarjetaRequest, Tarjeta } from '../../models/tarjeta.model';
import { TarjetaFormComponent } from '../../components/tarjeta-form/tarjeta-form.component';
import { TarjetaPreviewComponent } from '../../components/tarjeta-preview/tarjeta-preview.component';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { ToastService } from '../../../../shared/services/toast.service';
import { toAppHttpError } from '../../../../core/models/http-error.model';

@Component({
  selector: 'app-pagos-page',
  imports: [TarjetaFormComponent, TarjetaPreviewComponent, CardComponent, ButtonComponent, SpinnerComponent],
  templateUrl: './pagos-page.component.html',
  styleUrl: './pagos-page.component.scss'
})
export class PagosPageComponent implements OnInit {
  @ViewChild(TarjetaFormComponent) private readonly form?: TarjetaFormComponent;

  private readonly tarjetaApi = inject(TarjetaApiService);
  private readonly toastService = inject(ToastService);

  readonly tarjetas = signal<Tarjeta[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.tarjetaApi.listar().subscribe({
      next: (tarjetas) => {
        this.tarjetas.set(tarjetas);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.toastService.show(toAppHttpError(err).message, 'error');
      }
    });
  }

  guardar(request: GuardarTarjetaRequest): void {
    this.saving.set(true);
    this.tarjetaApi.guardar(request).subscribe({
      next: () => {
        this.saving.set(false);
        this.form?.reset();
        this.toastService.show('Tarjeta guardada.', 'success');
        this.cargar();
      },
      error: (err) => {
        this.saving.set(false);
        this.toastService.show(toAppHttpError(err).message, 'error');
      }
    });
  }

  vencimientoDe(tarjeta: Tarjeta): string {
    const mes = String(tarjeta.mesExpiracion).padStart(2, '0');
    const anio = String(tarjeta.anioExpiracion % 100).padStart(2, '0');
    return `${mes}/${anio}`;
  }

  eliminar(tarjeta: Tarjeta): void {
    this.tarjetaApi.eliminar(tarjeta.id).subscribe({
      next: () => {
        this.toastService.show('Tarjeta eliminada.', 'info');
        this.cargar();
      },
      error: (err) => this.toastService.show(toAppHttpError(err).message, 'error')
    });
  }
}
