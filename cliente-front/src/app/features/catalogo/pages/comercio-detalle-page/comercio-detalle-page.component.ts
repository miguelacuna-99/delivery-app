import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ComercioApiService } from '../../services/comercio-api.service';
import { Producto } from '../../models/producto.model';
import { CarritoApiService } from '../../../carrito/services/carrito-api.service';
import { ItemCarritoRequest } from '../../../carrito/models/carrito.model';
import { AuthService } from '../../../../core/services/auth.service';
import { toAppHttpError } from '../../../../core/models/http-error.model';
import { ProductoCardComponent } from '../../components/producto-card/producto-card.component';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-comercio-detalle-page',
  imports: [ProductoCardComponent, SpinnerComponent],
  templateUrl: './comercio-detalle-page.component.html',
  styleUrl: './comercio-detalle-page.component.scss'
})
export class ComercioDetallePageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly comercioApi = inject(ComercioApiService);
  private readonly carritoApi = inject(CarritoApiService);
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);

  readonly comercioId = this.route.snapshot.paramMap.get('comercioId') ?? '';
  readonly productos = signal<Producto[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly añadiendoId = signal<string | null>(null);

  ngOnInit(): void {
    this.comercioApi.catalogo(this.comercioId).subscribe({
      next: (productos) => {
        this.productos.set(productos);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(toAppHttpError(err).message);
        this.loading.set(false);
      }
    });
  }

  onAnadirAlCarrito(producto: Producto): void {
    if (!this.authService.isAuthenticated()) {
      this.toastService.show('Inicia sesion para añadir productos al carrito.', 'info');
      void this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }

    this.añadiendoId.set(producto.id);

    this.carritoApi.obtener().subscribe({
      next: (carrito) => {
        const mismoComercio = !carrito.comercioId || carrito.comercioId === this.comercioId;
        let items: ItemCarritoRequest[];

        if (mismoComercio) {
          const existentes: ItemCarritoRequest[] = (carrito.items ?? []).map((item) => ({
            productoId: item.productoId,
            cantidad: item.cantidad
          }));
          const idx = existentes.findIndex((item) => item.productoId === producto.id);
          if (idx >= 0) {
            existentes[idx] = { ...existentes[idx], cantidad: existentes[idx].cantidad + 1 };
          } else {
            existentes.push({ productoId: producto.id, cantidad: 1 });
          }
          items = existentes;
        } else {
          this.toastService.show('Tu carrito tenia productos de otro comercio; se ha reiniciado.', 'warning');
          items = [{ productoId: producto.id, cantidad: 1 }];
        }

        this.carritoApi
          .actualizar({
            comercioId: this.comercioId,
            items,
            codigoCupon: mismoComercio ? (carrito.codigoCupon ?? undefined) : undefined,
            puntosAplicados: mismoComercio ? carrito.puntosAplicados : undefined
          })
          .subscribe({
            next: () => {
              this.añadiendoId.set(null);
              this.toastService.show('Producto añadido al carrito.', 'success');
            },
            error: (err) => {
              this.añadiendoId.set(null);
              this.toastService.show(toAppHttpError(err).message, 'error');
            }
          });
      },
      error: (err) => {
        this.añadiendoId.set(null);
        this.toastService.show(toAppHttpError(err).message, 'error');
      }
    });
  }
}
