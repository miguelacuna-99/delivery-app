import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ContactoApiService } from '../../services/contacto-api.service';
import { toAppHttpError } from '../../../../core/models/http-error.model';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { InputComponent } from '../../../../shared/components/input/input.component';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { ToastService } from '../../../../shared/services/toast.service';

/**
 * Andamiaje: formulario simple para actualizar los datos de contacto del
 * cliente. No hay endpoint para leer los datos actuales, asi que el
 * formulario empieza vacio y solo se envian los campos rellenados.
 */
@Component({
  selector: 'app-contacto-page',
  imports: [FormsModule, ButtonComponent, InputComponent, CardComponent],
  templateUrl: './contacto-page.component.html',
  styleUrl: './contacto-page.component.scss'
})
export class ContactoPageComponent {
  private readonly contactoApi = inject(ContactoApiService);
  private readonly toastService = inject(ToastService);

  readonly mail = signal('');
  readonly direccionDomicilio = signal('');
  readonly telefono = signal('');
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  submit(): void {
    const req = {
      ...(this.mail() ? { mail: this.mail() } : {}),
      ...(this.direccionDomicilio() ? { direccionDomicilio: this.direccionDomicilio() } : {}),
      ...(this.telefono() ? { telefono: this.telefono() } : {})
    };

    if (Object.keys(req).length === 0) {
      this.errorMessage.set('Rellena al menos un campo para actualizar.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.contactoApi.actualizar(req).subscribe({
      next: () => {
        this.loading.set(false);
        this.toastService.show('Datos de contacto actualizados.', 'success');
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(toAppHttpError(err).message);
      }
    });
  }
}
