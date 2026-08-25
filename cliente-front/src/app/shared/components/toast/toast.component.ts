import { Component, computed, inject } from '@angular/core';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'ui-toast',
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.scss'
})
export class ToastComponent {
  protected readonly toastService = inject(ToastService);

  // Mas reciente primero, para que se apile hacia atras como una pila de cartas
  protected readonly toastsOrdenados = computed(() => [...this.toastService.toasts()].reverse());

  dismiss(id: number): void {
    this.toastService.dismiss(id);
  }
}
