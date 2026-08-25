export enum TipoUsuario {
  ROOT = 'ROOT',
  ADMIN = 'ADMIN',
  PERSONAL = 'PERSONAL',
  REPARTIDOR = 'REPARTIDOR',
  CLIENTE = 'CLIENTE'
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tipo: TipoUsuario;
  mustChangePassword: boolean;
}

export interface DecodedToken {
  userId: string;
  username: string;
  comercioId: string | null;
  tipo: TipoUsuario;
  exp: number;
}

export interface RegistroClienteRequest {
  comercioId: string;
  username: string;
  password: string;
  mail: string;
  direccionDomicilio: string;
  telefono: string;
}

export interface ClienteResponse {
  id: string;
  username: string;
  mail: string;
  direccionDomicilio: string;
  telefono: string;
}
