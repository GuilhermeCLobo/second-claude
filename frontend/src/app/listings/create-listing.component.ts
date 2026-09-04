import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { CATEGORIES, Category } from './category';
import { ListingsService } from './listings.service';

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
  selectedPhoto: File | null = null;

  constructor(
    private readonly listingsService: ListingsService,
    private readonly router: Router,
  ) {}

  onPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedPhoto = input.files?.[0] ?? null;
  }

  get canSubmit(): boolean {
    return this.form.valid && this.selectedPhoto !== null;
  }

  submit(): void {
    if (!this.selectedPhoto) {
      return;
    }
    this.errorMessage = null;
    const { title, description, price, category } = this.form.getRawValue();

    this.listingsService
      .create({ title, description, price: price as number, category: category as Category }, this.selectedPhoto)
      .subscribe({
        next: (listing) => {
          this.router.navigate(['/listings', listing.id]);
        },
        error: (error: HttpErrorResponse) => {
          this.errorMessage = error.error?.message ?? 'Could not create listing. Please try again.';
        },
      });
  }
}
