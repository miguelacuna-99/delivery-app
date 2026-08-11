export interface HistorialPuntosItem {
  fecha?: string;
  puntos?: number;
  motivo?: string;
  tipo?: string;
  pedidoId?: string;
}

export interface PuntosResponse {
  saldo: number;
  historial: HistorialPuntosItem[];
}
