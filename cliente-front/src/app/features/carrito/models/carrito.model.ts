/**
 * Item tal y como se envia en el PUT /api/carrito: solo producto + cantidad.
 * El precio nunca sale del carrito, lo reconstruye pedido-service desde el
 * catalogo en el checkout.
 */
export interface ItemCarritoRequest {
  productoId: string;
  cantidad: number;
}

export interface CarritoRequest {
  comercioId: string;
  items: ItemCarritoRequest[];
  codigoCupon?: string | null;
  puntosAplicados?: number;
}

/**
 * Respuesta de GET/PUT /api/carrito. El backend puede devolver un objeto
 * vacio si no hay carrito, y puede o no enriquecer los items con datos del
 * catalogo (nombre/precio) — por eso todo es opcional y tolerante.
 */
export interface ItemCarritoResponse {
  productoId: string;
  cantidad: number;
  nombre?: string;
  precio?: number;
  subtotal?: number;
}

export interface Carrito {
  comercioId?: string;
  items?: ItemCarritoResponse[];
  codigoCupon?: string | null;
  puntosAplicados?: number;
  total?: number;
  subtotal?: number;
  descuento?: number;
}
