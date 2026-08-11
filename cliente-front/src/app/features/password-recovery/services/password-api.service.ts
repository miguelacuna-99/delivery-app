import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PasswordApiService {
  private readonly http = inject(HttpClient);

  forgot(mail: string): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/auth/password/forgot`, { mail });
  }

  reset(token: string, nuevaPassword: string): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/auth/password/reset`, { token, nuevaPassword });
  }
}
