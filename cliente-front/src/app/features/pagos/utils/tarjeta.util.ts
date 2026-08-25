import { MarcaTarjeta } from '../models/tarjeta.model';

export function limpiarNumero(numero: string): string {
  return (numero ?? '').replace(/\D+/g, '');
}

export function formatearNumero(numero: string): string {
  const limpio = limpiarNumero(numero).slice(0, 19);
  return (limpio.match(/.{1,4}/g) ?? []).join(' ');
}

export function detectarMarca(numero: string): MarcaTarjeta {
  const limpio = limpiarNumero(numero);
  if (limpio.startsWith('4')) {
    return 'VISA';
  }
  const prefijo2 = Number(limpio.slice(0, 2));
  const prefijo4 = Number(limpio.slice(0, 4));
  if ((prefijo2 >= 51 && prefijo2 <= 55) || (prefijo4 >= 2221 && prefijo4 <= 2720)) {
    return 'MASTERCARD';
  }
  return 'OTRA';
}

/** Algoritmo de Luhn, validacion en cliente antes de enviar (el backend valida igual). */
export function pasaLuhn(numero: string): boolean {
  const limpio = limpiarNumero(numero);
  if (!/^\d{13,19}$/.test(limpio)) {
    return false;
  }
  let suma = 0;
  let doblar = false;
  for (let i = limpio.length - 1; i >= 0; i--) {
    let digito = Number(limpio[i]);
    if (doblar) {
      digito *= 2;
      if (digito > 9) {
        digito -= 9;
      }
    }
    suma += digito;
    doblar = !doblar;
  }
  return suma % 10 === 0;
}
