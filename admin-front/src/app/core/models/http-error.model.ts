export interface HttpErrorBody {
  status?: number;
  message?: string;
  error?: string;
  errors?: Record<string, string>;
}

export interface AppError {
  status: number;
  message: string;
}
