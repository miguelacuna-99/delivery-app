import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActualizarProductoRequest, CrearProductoRequest, Producto, ProductoApiService } from '../../services/producto-api.service';
import { InputComponent } from '../../../../shared/components/input/input.component';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { IconComponent } from '../../../../shared/components/icon/icon.component';
import { EditarProductoModalComponent } from '../../components/editar-producto-modal/editar-producto-modal.component';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-productos-page',
  standalone: true,
  imports: [
    CommonModule,
    InputComponent,
    ButtonComponent,
    CardComponent,
    SpinnerComponent,
    IconComponent,
    EditarProductoModalComponent
  ],
  template: `
    <div class="page-stack">
      <h2 class="page-title">Productos</h2>

      <app-card title="Nuevo producto" class="form-card productos-page__form">
        <app-input label="Nombre" [value]="nombre()" (valueChange)="nombre.set($event)"></app-input>
        <app-input label="Descripción" [value]="descripcion()" (valueChange)="descripcion.set($event)"></app-input>
        <app-input label="Ingredientes" [value]="ingredientes()" (valueChange)="ingredientes.set($event)"></app-input>
        <app-input label="URL de imagen" [value]="imagenUrl()" (valueChange)="imagenUrl.set($event)"></app-input>
        <app-input label="Precio" type="number" [value]="precio()" (valueChange)="precio.set($event)"></app-input>
        <app-button [loading]="saving()" (clicked)="crear()">Crear producto</app-button>
      </app-card>

      @if (loading()) {
        <div class="loading-state"><app-spinner></app-spinner></div>
      }

      @if (!loading() && productos().length === 0) {
        <div class="empty-state">
          <app-icon name="productos" [size]="32"></app-icon>
          <p>No hay productos todavía.</p>
        </div>
      }

      @if (productos().length > 0) {
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>Imagen</th>
                <th>Nombre</th>
                <th>Descripción</th>
                <th>Precio</th>
                <th>Disponible</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              @for (producto of productos(); track producto.id) {
                <tr>
                  <td>
                    @if (producto.imagenUrl) {
                      <img class="productos-page__thumb" [src]="producto.imagenUrl" alt="" />
                    }
                  </td>
                  <td>{{ producto.nombre }}</td>
                  <td>{{ producto.descripcion }}</td>
                  <td>{{ producto.precio | number: '1.2-2' }} €</td>
                  <td>
                    <span class="status-pill" [class]="producto.disponible ? 'status-pill--success' : 'status-pill--neutral'">
                      {{ producto.disponible ? 'Sí' : 'No' }}
                    </span>
                  </td>
                  <td class="productos-page__actions">
                    <app-button variant="ghost" (clicked)="abrirEdicion(producto)">Editar</app-button>
                    <app-button variant="ghost" (clicked)="toggleDisponible(producto)">
                      {{ producto.disponible ? 'Desactivar' : 'Activar' }}
                    </app-button>
                    <app-button variant="danger" (clicked)="eliminar(producto)">Eliminar</app-button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>

    <app-editar-producto-modal
      [visible]="modalVisible()"
      [saving]="savingEdicion()"
      [producto]="productoEditando()"
      (guardar)="guardarEdicion($event)"
      (cancelar)="cerrarEdicion()"
    ></app-editar-producto-modal>
  `,
  styleUrl: './productos-page.component.scss'
})
export class ProductosPageComponent {
  private readonly productoApi = inject(ProductoApiService);
  private readonly toastService = inject(ToastService);

  readonly productos = signal<Producto[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);

  readonly nombre = signal('');
  readonly descripcion = signal('');
  readonly ingredientes = signal('');
  readonly imagenUrl = signal('');
  readonly precio = signal('');

  readonly modalVisible = signal(false);
  readonly savingEdicion = signal(false);
  readonly productoEditando = signal<Producto | null>(null);

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.productoApi.listar().subscribe({
      next: (productos) => {
        this.productos.set(productos);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toastService.error('No se pudieron cargar los productos.');
      }
    });
  }

  crear(): void {
    const request: CrearProductoRequest = {
      nombre: this.nombre(),
      descripcion: this.descripcion(),
      ingredientes: this.ingredientes() || undefined,
      imagenUrl: this.imagenUrl() || undefined,
      precio: Number(this.precio()) || 0,
      disponible: true
    };
    if (!request.nombre) {
      this.toastService.error('Introduce un nombre de producto.');
      return;
    }
    this.saving.set(true);
    this.productoApi.crear(request).subscribe({
      next: () => {
        this.saving.set(false);
        this.nombre.set('');
        this.descripcion.set('');
        this.ingredientes.set('');
        this.imagenUrl.set('');
        this.precio.set('');
        this.toastService.success('Producto creado.');
        this.cargar();
      },
      error: () => {
        this.saving.set(false);
        this.toastService.error('No se pudo crear el producto.');
      }
    });
  }

  abrirEdicion(producto: Producto): void {
    this.productoEditando.set(producto);
    this.modalVisible.set(true);
  }

  cerrarEdicion(): void {
    this.modalVisible.set(false);
    this.productoEditando.set(null);
  }

  guardarEdicion(cambios: ActualizarProductoRequest): void {
    const producto = this.productoEditando();
    if (!producto) {
      return;
    }
    this.savingEdicion.set(true);
    this.productoApi
      .actualizar(producto.id, { ...cambios, disponible: producto.disponible })
      .subscribe({
        next: () => {
          this.savingEdicion.set(false);
          this.toastService.success('Producto actualizado.');
          this.cerrarEdicion();
          this.cargar();
        },
        error: () => {
          this.savingEdicion.set(false);
          this.toastService.error('No se pudo actualizar el producto.');
        }
      });
  }

  toggleDisponible(producto: Producto): void {
    this.productoApi.actualizarDisponibilidad(producto.id, !producto.disponible).subscribe({
      next: () => {
        this.toastService.success('Producto actualizado.');
        this.cargar();
      },
      error: () => this.toastService.error('No se pudo actualizar el producto.')
    });
  }

  eliminar(producto: Producto): void {
    this.productoApi.eliminar(producto.id).subscribe({
      next: () => {
        this.toastService.success('Producto eliminado.');
        this.cargar();
      },
      error: () => this.toastService.error('No se pudo eliminar el producto.')
    });
  }
}
