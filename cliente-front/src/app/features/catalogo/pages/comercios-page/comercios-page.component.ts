import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ComercioApiService } from '../../services/comercio-api.service';
import { Comercio } from '../../models/comercio.model';
import { toAppHttpError } from '../../../../core/models/http-error.model';
import { ComercioCardComponent } from '../../components/comercio-card/comercio-card.component';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';

@Component({
  selector: 'app-comercios-page',
  imports: [ComercioCardComponent, SpinnerComponent],
  templateUrl: './comercios-page.component.html',
  styleUrl: './comercios-page.component.scss'
})
export class ComerciosPageComponent implements OnInit {
  private readonly comercioApi = inject(ComercioApiService);
  private readonly router = inject(Router);

  readonly comercios = signal<Comercio[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.comercioApi.listar().subscribe({
      next: (comercios) => {
        this.comercios.set(comercios);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(toAppHttpError(err).message);
        this.loading.set(false);
      }
    });
  }

  onSeleccionar(comercio: Comercio): void {
    void this.router.navigate(['/catalogo', comercio.id]);
  }
}
