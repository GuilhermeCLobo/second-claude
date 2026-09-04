import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { CATEGORIES, Category } from './category';
import { ListingsService } from './listings.service';

const MAX_PHOTOS = 6;

@Component({
  selector: 'app-create-listing',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-listing.component.html',
})
export class CreateListingComponent {
  readonly categories = CATEGORIES;
  readonly form = new FormGroup({
    title: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    description: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    price: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0.01)] }),
    category: new FormControl<Category | ''>('', { nonNullable: true, validators: [Validators.required] }),
  });

  errorMessage: string | null = null;
  selectedPhotos: File[] = [];

  constructor(
    private readonly listingsService: ListingsService,
    private readonly router: Router,
  ) {}

  onPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedPhotos = Array.from(input.files ?? []).slice(0, MAX_PHOTOS);
  }

  get canSubmit(): boolean {
    return this.form.valid && this.selectedPhotos.length > 0;
  }

  submit(): void {
    if (this.selectedPhotos.length === 0) {
      return;
    }
    this.errorMessage = null;
    const { title, description, price, category } = this.form.getRawValue();
    const [firstPhoto, ...remainingPhotos] = this.selectedPhotos;

    this.listingsService
      .create({ title, description, price: price as number, category: category as Category }, firstPhoto)
      .subscribe({
        next: (listing) => this.addRemainingPhotos(listing.id, remainingPhotos),
        error: (error: HttpErrorResponse) => {
          this.errorMessage = error.error?.message ?? 'Could not create listing. Please try again.';
        },
      });
  }

  private addRemainingPhotos(listingId: number, photos: File[]): void {
    if (photos.length === 0) {
      this.router.navigate(['/listings', listingId]);
      return;
    }
    const [next, ...rest] = photos;
    this.listingsService.addPhoto(listingId, next).subscribe({
      next: () => this.addRemainingPhotos(listingId, rest),
      error: () => this.router.navigate(['/listings', listingId]),
    });
  }
}
