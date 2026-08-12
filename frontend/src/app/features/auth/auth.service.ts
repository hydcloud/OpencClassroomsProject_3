import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { LoginRequest } from '../../models/login-request';
import { LoginResponse } from '../../models/login-response';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly apiUrl = environment.apiUrl;
  private readonly tokenKey = 'datashare_token';
  private readonly emailKey = 'datashare_email';

  constructor(private http: HttpClient) { }

  login(loginRequest: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, loginRequest)
      .pipe(
        tap(response => {
          localStorage.setItem(
            this.tokenKey,
            response.token
          );

          localStorage.setItem(
            this.emailKey,
            response.email
          );
        })
      );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.emailKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  getEmail(): string | null {
    return localStorage.getItem(this.emailKey);
  }
}