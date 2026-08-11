import { ChangeDetectorRef, Component, OnInit } from '@angular/core';

import { FilesService } from '../files.service';
import { FileHistoryResponse } from '../../../models/file-history-response';

import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';

import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-files',
  imports: [FormsModule, DatePipe, RouterLink],
  templateUrl: './files.html',
  styleUrl: './files.scss',
})
export class Files implements OnInit {

  files: FileHistoryResponse[] = [];
  isLoading = false;
  errorMessage = '';
  selectedFile: File | null = null;
  expirationDays = 7;
  successMessage = '';
  isUploading = false;
  anonymousDownloadUrl = '';
  anonymousDownloadToken = '';
  deletingFileId: number | null = null;

  constructor(
    private readonly filesService: FilesService,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.loadFiles();
    }
  }

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  loadFiles(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.filesService.getHistory().subscribe({
      next: files => {
        this.files = files;
        this.isLoading = false;

        this.changeDetectorRef.markForCheck();
      },
      error: error => {
        console.error(error);

        this.errorMessage =
          'Impossible de charger les fichiers.';

        this.isLoading = false;

        this.changeDetectorRef.markForCheck();
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (!input.files || input.files.length === 0) {
      this.selectedFile = null;
      return;
    }
    this.selectedFile = input.files[0];
  }

  onUpload(): void {
    if (!this.selectedFile) {
      this.errorMessage = 'Sélectionnez un fichier.';
      return;
    }

    this.isUploading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const uploadRequest = this.authService.isAuthenticated()
      ? this.filesService.upload(
        this.selectedFile,
        this.expirationDays
      )
      : this.filesService.uploadAnonymous(
        this.selectedFile,
        this.expirationDays
      );

    uploadRequest.subscribe({
      next: response => {
        this.isUploading = false;
        this.successMessage = 'Fichier envoyé avec succès.';
        this.selectedFile = null;

        this.anonymousDownloadUrl =
          `${environment.apiUrl.replace('/api', '')}${response.downloadUrl}`;

        this.anonymousDownloadToken = response.downloadToken;

        if (this.authService.isAuthenticated()) {
          this.loadFiles();
        } else {
          this.anonymousDownloadUrl = response.downloadUrl;
        }

        this.changeDetectorRef.markForCheck();
      },

      error: error => {
        console.error(
          'Erreur lors de l’envoi du fichier :',
          error
        );

        this.isUploading = false;
        this.errorMessage =
          'Impossible d’envoyer le fichier.';
      }
    });
  }

  formatFileSize(size: number): string {
    if (size < 1024) {
      return `${size} octets`;
    }

    if (size < 1024 * 1024) {
      return `${(size / 1024).toFixed(1)} Ko`;
    }

    if (size < 1024 * 1024 * 1024) {
      return `${(size / (1024 * 1024)).toFixed(1)} Mo`;
    }

    return `${(size / (1024 * 1024 * 1024)).toFixed(1)} Go`;
  }

  getFileTypeLabel(mimeType: string): string {

    switch (mimeType) {

      case 'text/plain':
        return 'Fichier texte';

      case 'application/pdf':
        return 'Document PDF';

      case 'image/png':
        return 'Image PNG';

      case 'image/jpeg':
        return 'Image JPEG';

      default:
        return mimeType;

    }

  }

  onDelete(id: number): void {
    if (this.deletingFileId !== null) {
      return;
    }

    this.deletingFileId = id;
    this.errorMessage = '';
    this.successMessage = '';

    this.filesService.deleteFile(id).subscribe({
      next: () => {
        this.successMessage = 'Fichier supprimé avec succès.';
        this.deletingFileId = null;
        this.loadFiles();
        this.changeDetectorRef.markForCheck();
      },
      error: error => {
        console.error(
          'Erreur lors de la suppression du fichier :',
          error
        );

        this.errorMessage =
          error.status === 404
            ? 'Ce fichier avait déjà été supprimé.'
            : 'Impossible de supprimer le fichier.';

        this.deletingFileId = null;
        this.loadFiles();
      }
    });
  }

  onDownload(file: FileHistoryResponse): void {
    this.filesService
      .downloadFile(file.downloadToken)
      .subscribe({
        next: blob => {
          const url = URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = file.originalName;
          link.click();
          URL.revokeObjectURL(url);
        },
        error: error => {
          console.error(
            'Erreur lors du téléchargement :',
            error
          );

          this.errorMessage =
            'Impossible de télécharger le fichier.';
        }
      });
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  onAnonymousDownload(): void {
    this.filesService
      .downloadFile(this.anonymousDownloadToken)
      .subscribe({
        next: blob => {
          const url = URL.createObjectURL(blob);
          const link = document.createElement('a');

          link.href = url;
          link.download = this.selectedFile?.name ?? 'fichier';
          link.click();

          URL.revokeObjectURL(url);
        },

        error: error => {
          console.error(
            'Erreur lors du téléchargement :',
            error
          );

          this.errorMessage =
            'Impossible de télécharger le fichier.';
        }
      });
  }
}