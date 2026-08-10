import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { RegisterRequest } from '../../models/register-request';
import { UserResponse } from '../../models/user-response';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly apiUrl = `${environment.apiUrl}/user`;

  constructor(private http: HttpClient) {}

  register(registerRequest: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(
      `${environment.apiUrl}/user`,
      registerRequest
    );
  }
}