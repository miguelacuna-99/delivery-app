/**
 * Shape normalizado de error HTTP para mostrar en la UI, independientemente
 * de como venga formateado el error real desde el backend (gateway / Spring).
 */
export interface AppHttpError {
  status: number;
  message: string;
  raw?: unknown;
}

/**
 * Intenta extraer un mensaje legible de un error HTTP de Angular
 * (HttpErrorResponse) sin acoplarse rigidamente al shape del backend.
 */
export function toAppHttpError(error: unknown): AppHttpError {
  const anyError = error as {
    status?: number;
    error?: { message?: string; error?: string; detail?: string } | string;
    message?: string;
  };

  const status = typeof anyError?.status === 'number' ? anyError.status : 0;

  let message = 'Ha ocurrido un error inesperado.';

  if (typeof anyError?.error === 'string' && anyError.error.trim().length > 0) {
    message = anyError.error;
  } else if (anyError?.error && typeof anyError.error === 'object') {
    message = anyError.error.message ?? anyError.error.error ?? anyError.error.detail ?? message;
  } else if (anyError?.message) {
    message = anyError.message;
  }

  if (status === 0) {
    message = 'No se ha podido contactar con el servidor.';
  }

  return { status, message, raw: error };
}
