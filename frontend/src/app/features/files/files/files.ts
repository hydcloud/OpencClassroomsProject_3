import { ChangeDetectorRef, Component, OnInit } from '@angular/core';

import { FilesService } from '../files.service';
import { FileHistoryResponse } from '../../../models/file-history-response';

import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-files',
  imports: [FormsModule, DatePipe],
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
  deletingFileId: number | null = null;

  constructor(
    private readonly filesService: FilesService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.loadFiles();
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

    this.filesService
      .upload(this.selectedFile, this.expirationDays)
      .subscribe({
        next: () => {
          this.isUploading = false;
          this.successMessage = 'Fichier envoyé avec succès.';
          this.selectedFile = null;
          this.loadFiles();
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
}