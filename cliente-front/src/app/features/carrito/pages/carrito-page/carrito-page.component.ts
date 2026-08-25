import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { CarritoApiService } from '../../services/carrito-api.service';
import { Carrito, ItemCarritoRequest, ItemCarritoResponse } from '../../models/carrito.model';
import { ComercioApiService } from '../../../catalogo/services/comercio-api.service';
import { PuntosApiService } from '../../../puntos/services/puntos-api.service';
import { toAppHttpError } from '../../../../core/models/http-error.model';
import { CarritoItemRowComponent } from '../../components/carrito-item-row/carrito-item-row.component';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { InputComponent } from '../../../../shared/components/input/input.component';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-carrito-page',
  imports: [CarritoItemRowComponent, ButtonComponent, SpinnerComponent, CardComponent, InputComponent, RouterLink],
  templateUrl: './carrito-page.component.html',
  styleUrl: './carrito-page.component.scss'
})
export class CarritoPageComponent implements OnInit {
  private readonly carritoApi = inject(CarritoApiService);
  private readonly comercioApi = inject(ComercioApiService);
  private readonly puntosApi = inject(PuntosApiService);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);

  readonly carrito = signal<Carrito | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly saving = signal(false);

  // El backend no manda subtotal/descuento/total en el carrito (solo el
  // Pedido final los calcula): se recalculan aqui a partir de los items y
  // del porcentaje del cupon aplicado (ver aplicarCupon/quitarCupon).
  readonly porcentajeCupon = signal<number | null>(null);
  readonly codigoCuponInput = signal('');
  readonly aplicandoCupon = signal(false);

  // Canje de puntos: excluyente con el cupon (ver aplicarPuntos/aplicarCupon).
  readonly saldoPuntos = signal(0);
  readonly valorPuntoEuros = signal(0.01);
  readonly puntosInput = signal('');
  readonly aplicandoPuntos = signal(false);
  readonly descuentoPuntosEuros = signal<number | null>(null);

  readonly subtotal = computed(() =>
    (this.carrito()?.items ?? []).reduce((acc, item) => acc + (item.subtotal ?? 0), 0)
  );
  readonly descuento = computed(() => {
    if (this.carrito()?.puntosAplicados) {
      return this.descuentoPuntosEuros() ?? 0;
    }
    const porcentaje = this.porcentajeCupon();
    return porcentaje ? Math.round(this.subtotal() * porcentaje) / 100 : 0;
  });
  readonly total = computed(() => this.subtotal() - this.descuento());

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
                  imagenUrl: item.imagenUrl ?? producto?.imagenUrl,
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
          if (carrito.codigoCupon) {
            this.reaplicarCuponExistente(carrito.codigoCupon);
          }
          this.cargarPuntos(carrito);
        },
        error: (err) => {
          this.error.set(toAppHttpError(err).message);
          this.loading.set(false);
        }
      });
  }

  private cargarPuntos(carrito: Carrito): void {
    if (!carrito.comercioId) {
      return;
    }
    forkJoin({
      puntos: this.puntosApi.misPuntos(),
      comercio: this.comercioApi.obtener(carrito.comercioId)
    }).subscribe({
      next: ({ puntos, comercio }) => {
        this.saldoPuntos.set(puntos.saldo);
        this.valorPuntoEuros.set(comercio.valorPuntoEuros ?? 0.01);
        if (carrito.puntosAplicados) {
          this.descuentoPuntosEuros.set(carrito.puntosAplicados * this.valorPuntoEuros());
        }
      },
      error: () => {
        // No bloquea el carrito: si falla, simplemente no se puede canjear puntos ahora
      }
    });
  }

  /** Tras un refresco de pagina, recupera el porcentaje del cupon ya guardado en el carrito. */
  private reaplicarCuponExistente(codigo: string): void {
    this.carritoApi.aplicarCupon(codigo).subscribe({
      next: (res) => this.porcentajeCupon.set(res.porcentajeDescuento),
      error: () => {
        this.porcentajeCupon.set(null);
        this.carritoApi.quitarCupon().subscribe();
        this.toastService.show('El cupón ya no es válido y se ha quitado del carrito.', 'warning');
      }
    });
  }

  aplicarCupon(): void {
    const codigo = this.codigoCuponInput().trim();
    if (!codigo) {
      return;
    }
    this.aplicandoCupon.set(true);
    this.carritoApi.aplicarCupon(codigo).subscribe({
      next: (res) => {
        this.aplicandoCupon.set(false);
        this.porcentajeCupon.set(res.porcentajeDescuento);
        this.carrito.update((c) => (c ? { ...c, codigoCupon: res.codigo } : c));
        this.codigoCuponInput.set('');
        this.toastService.show('Cupón aplicado.', 'success');
      },
      error: (err) => {
        this.aplicandoCupon.set(false);
        this.toastService.show(toAppHttpError(err).message, 'error');
      }
    });
  }

  quitarCupon(): void {
    this.carritoApi.quitarCupon().subscribe({
      next: () => {
        this.porcentajeCupon.set(null);
        this.carrito.update((c) => (c ? { ...c, codigoCupon: null } : c));
        this.toastService.show('Cupón quitado.', 'info');
      },
      error: (err) => this.toastService.show(toAppHttpError(err).message, 'error')
    });
  }

  aplicarPuntos(): void {
    const puntos = Number(this.puntosInput());
    if (!puntos || puntos <= 0) {
      return;
    }
    if (puntos > this.saldoPuntos()) {
      this.toastService.show('No tienes suficientes puntos.', 'error');
      return;
    }
    this.aplicandoPuntos.set(true);
    this.carritoApi.aplicarPuntos(puntos).subscribe({
      next: (res) => {
        this.aplicandoPuntos.set(false);
        this.descuentoPuntosEuros.set(res.descuentoEuros);
        this.carrito.update((c) => (c ? { ...c, puntosAplicados: res.puntos } : c));
        this.puntosInput.set('');
        this.toastService.show('Puntos aplicados.', 'success');
      },
      error: (err) => {
        this.aplicandoPuntos.set(false);
        this.toastService.show(toAppHttpError(err).message, 'error');
      }
    });
  }

  quitarPuntos(): void {
    this.carritoApi.quitarPuntos().subscribe({
      next: () => {
        this.descuentoPuntosEuros.set(null);
        this.carrito.update((c) => (c ? { ...c, puntosAplicados: 0 } : c));
        this.toastService.show('Puntos quitados.', 'info');
      },
      error: (err) => this.toastService.show(toAppHttpError(err).message, 'error')
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
        items
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
