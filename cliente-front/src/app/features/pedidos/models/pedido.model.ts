export type EstadoPedido =
  | 'PENDIENTE'
  | 'ACEPTADO'
  | 'RECHAZADO'
  | 'PAGADO'
  | 'ENTREGADO'
  | 'CANCELADO'
  | 'PENDIENTE_DEVOLUCION'
  | 'DEVUELTO';

/** Shape del item dentro de un pedido; se mantiene tolerante porque el
 * contrato exacto de ItemPedidoPayload no forma parte de este frontend. */
export interface ItemPedido {
  productoId?: string;
  nombre?: string;
  cantidad?: number;
  precioUnitario?: number;
  subtotal?: number;
}

export interface Pedido {
  id: string;
  numeroPedido?: string;
  comercioId: string;
  estado: EstadoPedido;
  items: ItemPedido[];
  total: number;
  fechaCreacion: string | null;
  fechaAceptacion: string | null;
  fechaRechazo: string | null;
  fechaPago: string | null;
  fechaEntrega: string | null;
  fechaCancelacion: string | null;
  fechaAnulacion: string | null;
  fechaDevolucion: string | null;
}
