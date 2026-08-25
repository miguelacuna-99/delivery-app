export interface Producto {
  id: string;
  nombre: string;
  descripcion?: string;
  imagenUrl?: string;
  precio: number;
  disponible: boolean;
  comercioId: string;
}
