import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { CarritoApiService } from '../../services/carrito-api.service';
import { Carrito, ItemCarritoRequest, ItemCarritoResponse } from '../../models/carrito.model';
import { ComercioApiService } from '../../../catalogo/services/comercio-api.service';
import { toAppHttpError } from '../../../../core/models/http-error.model';
import { CarritoItemRowComponent } from '../../components/carrito-item-row/carrito-item-row.component';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-carrito-page',
  imports: [CarritoItemRowComponent, ButtonComponent, SpinnerComponent, CardComponent, RouterLink],
  templateUrl: './carrito-page.component.html',
  styleUrl: './carrito-page.component.scss'
})
export class CarritoPageComponent implements OnInit {
  private readonly carritoApi = inject(CarritoApiService);
  private readonly comercioApi = inject(ComercioApiService);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);

  readonly carrito = signal<Carrito | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly saving = signal(false);

  ngOnInit(): void {
    this.cargarCarrito();
  }

  private cargarCarrito(): void {
    this.loading.set(true);

    this.carritoApi
      .obtener()
      .pipe(
        switchMap((carrito) => {
          const items = carrito.items ?? [];
          const necesitaEnriquecer = carrito.comercioId && items.some((item) => item.precio === undefined);

          if (!necesitaEnriquecer || !carrito.comercioId) {
            return of(carrito);
          }

          return this.comercioApi.catalogo(carrito.comercioId).pipe(
            switchMap((productos) => {
              const enriquecidos: ItemCarritoResponse[] = items.map((item) => {
                const producto = productos.find((p) => p.id === item.productoId);
                const precio = item.precio ?? producto?.precio;
                return {
                  ...item,
                  nombre: item.nombre ?? producto?.nombre,
                  precio,
                  subtotal: item.subtotal ?? (precio !== undefined ? precio * item.cantidad : undefined)
                };
              });
              return of({ ...carrito, items: enriquecidos });
            })
          );
        })
      )
      .subscribe({
        next: (carrito) => {
          this.carrito.set(carrito);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(toAppHttpError(err).message);
          this.loading.set(false);
        }
      });
  }

  private guardarItems(items: ItemCarritoRequest[]): void {
    const carritoActual = this.carrito();
    if (!carritoActual?.comercioId) {
      return;
    }

    if (items.length === 0) {
      this.vaciar();
      return;
    }

    this.saving.set(true);

    this.carritoApi
      .actualizar({
        comercioId: carritoActual.comercioId,
        items,
        codigoCupon: carritoActual.codigoCupon ?? undefined,
        puntosAplicados: carritoActual.puntosAplicados
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.cargarCarrito();
        },
        error: (err) => {
          this.saving.set(false);
          this.toastService.show(toAppHttpError(err).message, 'error');
        }
      });
  }

  onCantidadChange(productoId: string, cantidad: number): void {
    const items = (this.carrito()?.items ?? []).map((item) =>
      item.productoId === productoId ? { productoId: item.productoId, cantidad } : { productoId: item.productoId, cantidad: item.cantidad }
    );
    this.guardarItems(items);
  }

  onEliminar(productoId: string): void {
    const items = (this.carrito()?.items ?? [])
      .filter((item) => item.productoId !== productoId)
      .map((item) => ({ productoId: item.productoId, cantidad: item.cantidad }));
    this.guardarItems(items);
  }

  vaciar(): void {
    this.saving.set(true);
    this.carritoApi.vaciar().subscribe({
      next: () => {
        this.saving.set(false);
        this.carrito.set(null);
        this.toastService.show('Carrito vaciado.', 'info');
      },
      error: (err) => {
        this.saving.set(false);
        this.toastService.show(toAppHttpError(err).message, 'error');
      }
    });
  }

  irACheckout(): void {
    void this.router.navigateByUrl('/checkout');
  }
}
