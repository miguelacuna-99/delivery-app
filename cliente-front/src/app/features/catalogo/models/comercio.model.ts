/**
 * Modelo tolerante a campos extra: solo se declaran los campos que la UI
 * usa realmente, el resto de lo que devuelva el backend simplemente se
 * ignora (TypeScript no hace excess-property-check sobre datos que llegan
 * por HttpClient, asi que no hace falta index signature).
 */
export interface Comercio {
  id: string;
  nombre: string;
  direccion?: string;
  telefono?: string;
  email?: string;
}
