import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: number;
  message: string;
  type: ToastType;
  removing?: boolean;
}

const DEFAULT_DURATION_MS = 4000;
// Debe coincidir con la duracion de la animacion de salida en toast.component.scss
export const TOAST_EXIT_DURATION_MS = 200;

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly toastsSignal = signal<Toast[]>([]);
  private nextId = 1;

  readonly toasts = this.toastsSignal.asReadonly();

  /** durationMs = 0 deja el toast fijo hasta que se descarte a mano. */
  show(message: string, type: ToastType = 'info', durationMs = DEFAULT_DURATION_MS): number {
    const id = this.nextId++;
    this.toastsSignal.update((toasts) => [...toasts, { id, message, type }]);
    if (durationMs > 0) {
      setTimeout(() => this.dismiss(id), durationMs);
    }
    return id;
  }

  success(message: string, durationMs?: number): void {
    this.show(message, 'success', durationMs);
  }

  error(message: string, durationMs?: number): void {
    this.show(message, 'error', durationMs);
  }

  warning(message: string, durationMs?: number): void {
    this.show(message, 'warning', durationMs);
  }

  info(message: string, durationMs?: number): void {
    this.show(message, 'info', durationMs);
  }

  /** Encadena estados de carga/éxito/error de una promesa en un unico toast. */
  async promise<T>(
    promise: Promise<T>,
    messages: {
      loading: string;
      success: string | ((value: T) => string);
      error: string | ((err: unknown) => string);
    }
  ): Promise<T> {
    const id = this.show(messages.loading, 'info', 0);
    try {
      const value = await promise;
      this.dismiss(id);
      this.show(typeof messages.success === 'function' ? messages.success(value) : messages.success, 'success');
      return value;
    } catch (err) {
      this.dismiss(id);
      this.show(typeof messages.error === 'function' ? messages.error(err) : messages.error, 'error');
      throw err;
    }
  }

  dismiss(id: number): void {
    const toast = this.toastsSignal().find((t) => t.id === id);
    if (!toast || toast.removing) {
      return;
    }
    // Marca la salida para que el componente anime antes de quitarlo del DOM
    this.toastsSignal.update((toasts) => toasts.map((t) => (t.id === id ? { ...t, removing: true } : t)));
    setTimeout(() => {
      this.toastsSignal.update((toasts) => toasts.filter((t) => t.id !== id));
    }, TOAST_EXIT_DURATION_MS);
  }
}
