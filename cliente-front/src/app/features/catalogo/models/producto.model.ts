export interface Producto {
  id: string;
  nombre: string;
  descripcion?: string;
  precio: number;
  disponible: boolean;
  comercioId: string;
}
