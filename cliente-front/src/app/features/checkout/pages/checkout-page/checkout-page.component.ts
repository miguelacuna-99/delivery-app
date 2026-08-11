import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CheckoutApiService } from '../../services/checkout-api.service';
import { toAppHttpError } from '../../../../core/models/http-error.model';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { CardComponent } from '../../../../shared/components/card/card.component';

@Component({
  selector: 'app-checkout-page',
  imports: [RouterLink, ButtonComponent, CardComponent],
  templateUrl: './checkout-page.component.html',
  styleUrl: './checkout-page.component.scss'
})
export class CheckoutPageComponent {
  private readonly checkoutApi = inject(CheckoutApiService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly errorStatus = signal<number | null>(null);

  confirmar(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.errorStatus.set(null);

    this.checkoutApi.checkout().subscribe({
      next: (pedido) => {
        this.loading.set(false);
        void this.router.navigate(['/pedidos'], { queryParams: { nuevo: pedido.id } });
      },
      error: (err) => {
        this.loading.set(false);
        const appError = toAppHttpError(err);
        this.errorMessage.set(appError.message);
        this.errorStatus.set(appError.status);
      }
    });
  }
}
