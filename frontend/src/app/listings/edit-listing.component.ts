import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { CATEGORIES, Category } from './category';
import { ListingsService } from './listings.service';

@Component({
  selector: 'app-edit-listing',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-listing.component.html',
})
export class EditListingComponent implements OnInit {
  readonly categories = CATEGORIES;
  readonly form = new FormGroup({
    title: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    description: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    price: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0.01)] }),
    category: new FormControl<Category | ''>('', { nonNullable: true, validators: [Validators.required] }),
  });

  errorMessage: string | null = null;
  notFound = false;
  private listingId!: number;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly listingsService: ListingsService,
  ) {}

  ngOnInit(): void {
    this.listingId = Number(this.route.snapshot.paramMap.get('id'));
    this.listingsService.getById(this.listingId).subscribe({
      next: (listing) => {
        this.form.setValue({
          title: listing.title,
          description: listing.description,
          price: listing.price,
          category: listing.category,
        });
      },
      error: () => {
        this.notFound = true;
      },
    });
  }

  submit(): void {
    if (!this.form.valid) {
      return;
    }
    this.errorMessage = null;
    const { title, description, price, category } = this.form.getRawValue();

    this.listingsService
      .update(this.listingId, { title, description, price: price as number, category: category as Category })
      .subscribe({
        next: (listing) => {
          this.router.navigate(['/listings', listing.id]);
        },
        error: (error: HttpErrorResponse) => {
          this.errorMessage = error.error?.message ?? 'Could not update listing. Please try again.';
        },
      });
  }
}
