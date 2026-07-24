import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../../environments/environment';
@Injectable({
  providedIn: 'root'
})
export class FileService {

  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getFiles() {
    return this.http.get(`${this.apiUrl}/files`);
  }

  upload(formData: FormData) {
    return this.http.post(`${this.apiUrl}/files`, formData);
  }
}