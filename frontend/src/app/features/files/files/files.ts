import { DatePipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { DownloadFileInfoResponse } from '../../../models/download-file-info-response';
import { FileHistoryResponse } from '../../../models/file-history-response';
import { AuthService } from '../../auth/auth.service';
import { FilesService } from '../files.service';

type FileFilter = 'all' | 'active' | 'expired';

@Component({
  selector: 'app-files',
  imports: [FormsModule, DatePipe, RouterLink],
  templateUrl: './files.html',
  styleUrl: './files.scss',
})
export class Files implements OnInit {
  files: FileHistoryResponse[] = [];
  fileFilter: FileFilter = 'all';
  isLoading = false;
  deletingFileId: number | null = null;
  selectedFile: File | null = null;
  expirationDays = 7;
  password = '';
  isUploading = false;
  errorMessage = '';
  successMessage = '';
  anonymousDownloadUrl = '';
  anonymousDownloadToken = '';
  anonymousPasswordProtected = false;
  downloadPassword = '';
  protectedFileToken: string | null = null;
  protectedFileName = '';
  isDownloadPage = false;
  downloadFileInfo: DownloadFileInfoResponse | null = null;
  isDownloadInfoLoading = false;
  showAccountUpload = false;
  uploadErrorMessage = '';
  fileActionErrorMessage = '';
  fileActionSuccessMessage = '';

  constructor(
    private readonly filesService: FilesService,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly changeDetectorRef: ChangeDetectorRef,
  ) { }

  ngOnInit(): void {
    const token = this.route.snapshot.paramMap.get('token');

    if (token) {
      this.isDownloadPage = true;
      this.anonymousDownloadToken = token;
      this.loadDownloadFileInfo(token);
      return;
    }

    if (this.authService.isAuthenticated()) {
      this.showAccountUpload = false;
      this.loadFiles();
    }
  }

  get userEmail(): string | null {
    return this.authService.getEmail();
  }

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  isExpired(file: FileHistoryResponse): boolean {
    return new Date(file.expiresAt).getTime() < Date.now();
  }

  get filteredFiles(): FileHistoryResponse[] {
    switch (this.fileFilter) {
      case 'active':
        return this.files.filter(file => !this.isExpired(file));
      case 'expired':
        return this.files.filter(file => this.isExpired(file));
      default:
        return this.files;
    }
  }

  loadFiles(): void {
    this.isLoading = true;

    this.filesService.getHistory().subscribe({
      next: files => {
        this.files = files;
        this.isLoading = false;
        this.changeDetectorRef.markForCheck();
      },
      error: () => {
        this.fileActionErrorMessage = 'Impossible de charger les fichiers.';
        this.isLoading = false;
        this.changeDetectorRef.markForCheck();
      },
    });
  }

  openAccountUpload(): void {
    this.uploadErrorMessage = '';
    this.selectedFile = null;
    this.password = '';
    this.showAccountUpload = true;
  }

  closeAccountUpload(): void {
    if (this.isUploading) {
      return;
    }

    this.showAccountUpload = false;
    this.uploadErrorMessage = '';
    this.selectedFile = null;
    this.password = '';
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (!input.files?.length) {
      this.selectedFile = null;
      return;
    }

    this.selectedFile = input.files[0];
    this.uploadErrorMessage = '';
  }

  onUpload(): void {
    const authenticated = this.authService.isAuthenticated();

    if (!this.selectedFile) {
      if (authenticated) {
        this.uploadErrorMessage = 'Sélectionnez un fichier.';
      } else {
        this.errorMessage = 'Sélectionnez un fichier.';
      }
      return;
    }

    this.isUploading = true;
    this.anonymousDownloadUrl = '';

    if (authenticated) {
      this.uploadErrorMessage = '';
      this.fileActionErrorMessage = '';
      this.fileActionSuccessMessage = '';
    } else {
      this.errorMessage = '';
      this.successMessage = '';
    }

    const uploadRequest = authenticated
      ? this.filesService.upload(
          this.selectedFile,
          this.expirationDays,
          this.password,
        )
      : this.filesService.uploadAnonymous(
          this.selectedFile,
          this.expirationDays,
          this.password,
        );

    uploadRequest.subscribe({
      next: response => {
        this.isUploading = false;
        this.selectedFile = null;
        this.password = '';

        if (authenticated) {
          this.showAccountUpload = false;
          this.uploadErrorMessage = '';
          this.fileActionSuccessMessage = 'Fichier téléversé avec succès.';
          this.loadFiles();
        } else {
          this.successMessage = 'Fichier envoyé avec succès.';
          this.anonymousDownloadUrl =
            `${window.location.origin}/download/${response.downloadToken}`;
          this.anonymousDownloadToken = response.downloadToken;
          this.anonymousPasswordProtected = response.passwordProtected;
          this.protectedFileToken = response.downloadToken;
          this.protectedFileName = response.originalName;
        }

        this.changeDetectorRef.markForCheck();
      },
      error: error => {
        this.isUploading = false;

        const backendMessage =
          typeof error.error?.message === 'string'
            ? error.error.message
            : 'Impossible d’envoyer le fichier.';

        if (authenticated) {
          this.uploadErrorMessage = backendMessage;
        } else {
          this.errorMessage = backendMessage;
        }

        this.changeDetectorRef.markForCheck();
      },
    });
  }

  onDelete(id: number): void {
    if (this.deletingFileId !== null) {
      return;
    }

    this.deletingFileId = id;
    this.fileActionErrorMessage = '';
    this.fileActionSuccessMessage = '';

    this.filesService.deleteFile(id).subscribe({
      next: () => {
        this.fileActionSuccessMessage = 'Fichier supprimé avec succès.';
        this.deletingFileId = null;
        this.loadFiles();
      },
      error: error => {
        this.fileActionErrorMessage =
          error.status === 404
            ? 'Ce fichier avait déjà été supprimé.'
            : 'Impossible de supprimer le fichier.';

        this.deletingFileId = null;
        this.loadFiles();
        this.changeDetectorRef.markForCheck();
      },
    });
  }

  onDownload(file: FileHistoryResponse): void {
    this.fileActionErrorMessage = '';
    this.fileActionSuccessMessage = '';
    this.downloadPassword = '';

    if (file.passwordProtected) {
      this.protectedFileToken = file.downloadToken;
      this.protectedFileName = file.originalName;
      return;
    }

    // Le token et le nom doivent venir du fichier sélectionné dans l'historique.
    this.downloadFile(file.downloadToken, file.originalName);
  }

  onDownloadClick(file: FileHistoryResponse): void {
    const passwordFieldIsOpen =
      file.passwordProtected &&
      this.protectedFileToken === file.downloadToken;

    if (passwordFieldIsOpen) {
      this.onProtectedDownload();
      return;
    }

    this.onDownload(file);
  }

  onProtectedDownload(): void {
    if (!this.protectedFileToken) {
      return;
    }

    if (this.authService.isAuthenticated() && !this.isDownloadPage) {
      this.fileActionErrorMessage = '';
      this.fileActionSuccessMessage = '';
    } else {
      this.errorMessage = '';
    }

    this.downloadFile(
      this.protectedFileToken,
      this.protectedFileName,
      this.downloadPassword,
    );
  }

  onAnonymousDownload(): void {
    if (!this.anonymousDownloadToken) {
      return;
    }

    this.downloadFile(
      this.anonymousDownloadToken,
      this.protectedFileName || 'fichier',
    );
  }

  onSharedDownload(): void {
    if (!this.anonymousDownloadToken) {
      return;
    }

    this.errorMessage = '';

    this.downloadFile(
      this.anonymousDownloadToken,
      this.downloadFileInfo?.originalName ?? 'fichier',
      this.downloadPassword.trim() || undefined,
    );
  }

  private downloadFile(
    token: string,
    originalName: string,
    password?: string,
  ): void {
    this.filesService.downloadFile(token, password).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');

        link.href = url;
        link.download = originalName;
        link.click();

        URL.revokeObjectURL(url);

        this.downloadPassword = '';
        this.protectedFileToken = null;
        this.protectedFileName = '';

        this.changeDetectorRef.markForCheck();
      },
      error: error => {
        const message =
          error.status === 403
            ? 'Mot de passe incorrect.'
            : 'Impossible de télécharger le fichier.';

        if (this.authService.isAuthenticated() && !this.isDownloadPage) {
          this.fileActionErrorMessage = message;
          this.fileActionSuccessMessage = '';
        } else {
          this.errorMessage = message;
        }

        this.changeDetectorRef.markForCheck();
      },
    });
  }

  async onShare(file: FileHistoryResponse): Promise<void> {
    const url = `${window.location.origin}/download/${file.downloadToken}`;

    this.fileActionErrorMessage = '';
    this.fileActionSuccessMessage = '';

    try {
      if (navigator.share) {
        await navigator.share({
          title: file.originalName,
          text: 'Un fichier vous a été partagé via DataShare.',
          url,
        });
        return;
      }

      await navigator.clipboard.writeText(url);
      this.fileActionSuccessMessage =
        'Lien de partage copié dans le presse-papiers.';
    } catch (error) {
      if (error instanceof DOMException && error.name === 'AbortError') {
        return;
      }

      this.fileActionErrorMessage = 'Impossible de partager le lien.';
    }

    this.changeDetectorRef.markForCheck();
  }

  loadDownloadFileInfo(token: string): void {
    this.isDownloadInfoLoading = true;
    this.errorMessage = '';

    this.filesService.getFileInfo(token).subscribe({
      next: info => {
        this.downloadFileInfo = info;
        this.isDownloadInfoLoading = false;
        this.changeDetectorRef.markForCheck();
      },
      error: error => {
        this.isDownloadInfoLoading = false;

        if (error.status === 410) {
          this.errorMessage = 'Ce lien de téléchargement a expiré.';
        } else if (error.status === 404) {
          this.errorMessage = 'Ce lien de téléchargement est invalide.';
        } else {
          this.errorMessage =
            'Impossible de charger les informations du fichier.';
        }

        this.changeDetectorRef.markForCheck();
      },
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
    const types: Record<string, string> = {
      'application/pdf': 'PDF',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document':
        'DOCX',
      'application/msword': 'DOC',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet':
        'XLSX',
      'application/vnd.ms-excel': 'XLS',
      'image/jpeg': 'JPG',
      'image/png': 'PNG',
      'text/plain': 'TXT',
      'application/zip': 'ZIP',
    };

    return types[mimeType] ?? 'Fichier';
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}