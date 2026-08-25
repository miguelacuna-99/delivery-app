/**
 * Item tal y como se envia en el PUT /api/carrito: solo producto + cantidad.
 * El precio nunca sale del carrito, lo reconstruye pedido-service desde el
 * catalogo en el checkout.
 */
export interface ItemCarritoRequest {
  productoId: string;
  cantidad: number;
}

// El cupon y los puntos se gestionan solo via POST/DELETE /api/carrito/cupon
// y /api/carrito/puntos: el PUT de items los ignora si vienen en el body.
export interface CarritoRequest {
  comercioId: string;
  items: ItemCarritoRequest[];
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
  imagenUrl?: string;
  precio?: number;
  subtotal?: number;
}

export interface Carrito {
  comercioId?: string;
  items?: ItemCarritoResponse[];
  codigoCupon?: string | null;
  puntosAplicados?: number;
}

export interface AplicarCuponResponse {
  codigo: string;
  porcentajeDescuento: number;
}

export interface AplicarPuntosResponse {
  puntos: number;
  descuentoEuros: number;
}
