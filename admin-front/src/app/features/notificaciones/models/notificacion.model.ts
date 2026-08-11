export enum TipoNotificacion {
  PEDIDO_PENDIENTE = 'PEDIDO_PENDIENTE',
  PEDIDO_PAGADO = 'PEDIDO_PAGADO'
}

export interface Notificacion {
  id: string;
  tipo: TipoNotificacion;
  mensaje: string;
  leida: boolean;
  fechaCreacion?: string;
  [key: string]: unknown;
}
