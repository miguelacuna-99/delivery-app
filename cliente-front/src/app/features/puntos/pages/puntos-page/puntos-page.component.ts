import { Component, OnInit, inject, signal } from '@angular/core';
import { PuntosApiService } from '../../services/puntos-api.service';
import { HistorialPuntosItem } from '../../models/puntos.model';
import { toAppHttpError } from '../../../../core/models/http-error.model';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';

/**
 * Andamiaje: pinta saldo + historial de puntos de forma simple, sin pulir.
 */
@Component({
  selector: 'app-puntos-page',
  imports: [SpinnerComponent],
  templateUrl: './puntos-page.component.html',
  styleUrl: './puntos-page.component.scss'
})
export class PuntosPageComponent implements OnInit {
  private readonly puntosApi = inject(PuntosApiService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly saldo = signal(0);
  readonly historial = signal<HistorialPuntosItem[]>([]);

  ngOnInit(): void {
    this.puntosApi.misPuntos().subscribe({
      next: (res) => {
        this.saldo.set(res.saldo);
        this.historial.set(res.historial ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(toAppHttpError(err).message);
        this.loading.set(false);
      }
    });
  }
}
