export enum EstadoPedido {
  PENDIENTE = 'PENDIENTE',
  ACEPTADO = 'ACEPTADO',
  RECHAZADO = 'RECHAZADO',
  PAGADO = 'PAGADO',
  ENTREGADO = 'ENTREGADO',
  CANCELADO = 'CANCELADO',
  PENDIENTE_DEVOLUCION = 'PENDIENTE_DEVOLUCION',
  DEVUELTO = 'DEVUELTO'
}

export interface ItemPedido {
  productoId?: string;
  nombre?: string;
  cantidad: number;
  precio?: number;
  [key: string]: unknown;
}

export interface Pedido {
  id: string;
  numeroPedido: string;
  comercioId: string;
  estado: EstadoPedido;
  items: ItemPedido[];
  total: number;
  fechaCreacion?: string;
  fechaAceptacion?: string;
  fechaRechazo?: string;
  fechaPago?: string;
  fechaEntrega?: string;
  fechaCancelacion?: string;
  fechaAnulacion?: string;
  fechaDevolucion?: string;
}

export interface AceptarPedidoRequest {
  tiempoEstimadoMin: number;
}

export interface RechazarPedidoRequest {
  mensaje?: string;
}

export interface EntregarPedidoRequest {
  numeroPedido: string;
}

export interface AnularPedidoRequest {
  motivo: string;
}
