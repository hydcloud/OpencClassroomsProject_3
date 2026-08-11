import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { FileHistoryResponse } from '../../models/file-history-response';
import { FileUploadResponse } from '../../models/file-upload-response';

@Injectable({
  providedIn: 'root'
})
export class FilesService {
  private readonly apiUrl = `${environment.apiUrl}/files`;

  constructor(private readonly http: HttpClient) { }

  getHistory(): Observable<FileHistoryResponse[]> {
    return this.http.get<FileHistoryResponse[]>(this.apiUrl);
  }

  upload(
    file: File,
    expirationDays: number
  ): Observable<FileUploadResponse> {
    return this.sendFile(
      this.apiUrl,
      file,
      expirationDays
    );
  }

  uploadAnonymous(
    file: File,
    expirationDays: number
  ): Observable<FileUploadResponse> {
    return this.sendFile(
      `${this.apiUrl}/anonymous`,
      file,
      expirationDays
    );
  }

  private sendFile(
    url: string,
    file: File,
    expirationDays: number
  ): Observable<FileUploadResponse> {
    const formData = new FormData();

    formData.append('file', file);
    formData.append(
      'expirationDays',
      expirationDays.toString()
    );

    return this.http.post<FileUploadResponse>(
      url,
      formData
    );
  }

  deleteFile(
    id: number
  ):
    Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${id}`
    )
  }

  downloadFile(
    token: string
  ):
    Observable<Blob> {
    return this.http.get(
      `${this.apiUrl}/${token}/file`,
      {
        responseType: 'blob'
      }
    );
  }
}