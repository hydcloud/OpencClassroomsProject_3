export interface FileHistoryResponse {
  id: number;
  originalName: string;
  mimeType: string;
  size: number;
  uploadedAt: string;
  expiresAt: string;
  downloadToken: string;
}