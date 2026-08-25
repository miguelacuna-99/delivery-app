export type MarcaTarjeta = 'VISA' | 'MASTERCARD' | 'OTRA';

export interface Tarjeta {
  id: string;
  titular: string;
  marca: MarcaTarjeta;
  ultimos4: string;
  mesExpiracion: number;
  anioExpiracion: number;
  [key: string]: unknown;
}

/** El CVV nunca viaja al backend: no hace falta, no hay cobro real. */
export interface GuardarTarjetaRequest {
  numero: string;
  titular: string;
  mesExpiracion: number;
  anioExpiracion: number;
}
