import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'info' | 'warning';

export interface Toast {
  id: number;
  message: string;
  type: ToastType;
}

const DEFAULT_DURATION_MS = 4000;

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly toastsSignal = signal<Toast[]>([]);
  private nextId = 1;

  readonly toasts = this.toastsSignal.asReadonly();

  show(message: string, type: ToastType = 'info', durationMs = DEFAULT_DURATION_MS): number {
    const id = this.nextId++;
    this.toastsSignal.update((current) => [...current, { id, message, type }]);

    if (durationMs > 0) {
      setTimeout(() => this.dismiss(id), durationMs);
    }

    return id;
  }

  dismiss(id: number): void {
    this.toastsSignal.update((current) => current.filter((toast) => toast.id !== id));
  }
}
