/**
 * Helpers puramente visuales para el tile de iniciales que sustituye a las
 * fotos de producto/comercio (el backend no expone URLs de imagen). No
 * contienen logica de negocio: solo derivan texto/clase a partir de datos
 * que ya vienen en el modelo.
 */

const AVATAR_CLASES = ['avatar-tile--blue', 'avatar-tile--yellow', 'avatar-tile--neutral'] as const;

/** Iniciales (hasta 2 letras) a partir de un nombre, en mayusculas. */
export function iniciales(nombre: string): string {
  const palabras = nombre.trim().split(/\s+/).filter(Boolean);
  if (palabras.length === 0) {
    return '?';
  }
  if (palabras.length === 1) {
    return palabras[0].slice(0, 2).toUpperCase();
  }
  return (palabras[0][0] + palabras[1][0]).toUpperCase();
}

/** Clase de color determinista segun el id, para que cada tarjeta sea estable entre renders. */
export function avatarClase(id: string): string {
  let hash = 0;
  for (let i = 0; i < id.length; i++) {
    hash = (hash * 31 + id.charCodeAt(i)) >>> 0;
  }
  return AVATAR_CLASES[hash % AVATAR_CLASES.length];
}
