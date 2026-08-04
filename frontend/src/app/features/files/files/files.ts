import { Component, OnInit } from '@angular/core';

import { FilesService } from '../files.service';
import { FileHistoryResponse } from '../../../models/file-history-response';

import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-files',
  imports: [FormsModule],
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

  constructor(
    private readonly filesService: FilesService
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
      },

      error: error => {
        console.error(error);
        this.errorMessage =
          'Impossible de charger les fichiers.';
        this.isLoading = false;

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

}