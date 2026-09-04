import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '../auth/auth.service';
import { CATEGORIES } from './category';
import { Listing } from './listing.model';
import { ListingsService } from './listings.service';

const MAX_PHOTOS = 6;

@Component({
  selector: 'app-listing-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './listing-detail.component.html',
})
export class ListingDetailComponent implements OnInit {
  readonly categories = CATEGORIES;
  readonly maxPhotos = MAX_PHOTOS;
  listing: Listing | null = null;
  notFound = false;
  deleteError = '';
  buyError = '';
  favoriteError = '';
  addPhotoError = '';
  removePhotoError = '';
  reorderError = '';
  selectedNewPhoto: File | null = null;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly listingsService: ListingsService,
    private readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.listingsService.getById(id).subscribe({
      next: (listing) => {
        this.listing = listing;
      },
      error: () => {
        this.notFound = true;
      },
    });
  }

  categoryLabel(): string {
    return this.categories.find((category) => category.value === this.listing?.category)?.label ?? '';
  }

  canDelete(): boolean {
    return (
      !!this.listing &&
      this.listing.status === 'ACTIVE' &&
      this.listing.ownerId === this.authService.session()?.userId
    );
  }

  canEdit(): boolean {
    return (
      !!this.listing &&
      this.listing.status === 'ACTIVE' &&
      this.listing.ownerId === this.authService.session()?.userId
    );
  }

  canManagePhotos(): boolean {
    return this.canEdit();
  }

  edit(): void {
    if (!this.listing) {
      return;
    }
    this.router.navigate(['/listings', this.listing.id, 'edit']);
  }

  delete(): void {
    if (!this.listing) {
      return;
    }
    this.deleteError = '';
    this.listingsService.delete(this.listing.id).subscribe({
      next: () => {
        this.router.navigateByUrl('/');
      },
      error: () => {
        this.deleteError = 'Could not delete this listing.';
      },
    });
  }

  canBuy(): boolean {
    const userId = this.authService.session()?.userId;
    return !!this.listing && this.listing.status === 'ACTIVE' && !!userId && this.listing.ownerId !== userId;
  }

  buy(): void {
    if (!this.listing) {
      return;
    }
    this.buyError = '';
    this.listingsService.buy(this.listing.id).subscribe({
      next: (listing) => {
        this.listing = listing;
      },
      error: () => {
        this.buyError = 'Could not buy this listing.';
      },
    });
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  toggleFavorite(): void {
    if (!this.listing) {
      return;
    }
    this.favoriteError = '';
    if (this.listing.favorited) {
      this.listingsService.unfavorite(this.listing.id).subscribe({
        next: () => {
          if (this.listing) {
            this.listing.favorited = false;
          }
        },
        error: () => {
          this.favoriteError = 'Could not update favorite.';
        },
      });
    } else {
      this.listingsService.favorite(this.listing.id).subscribe({
        next: (listing) => {
          this.listing = listing;
        },
        error: () => {
          this.favoriteError = 'Could not update favorite.';
        },
      });
    }
  }

  onNewPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedNewPhoto = input.files?.[0] ?? null;
  }

  addPhoto(): void {
    if (!this.listing || !this.selectedNewPhoto) {
      return;
    }
    this.addPhotoError = '';
    this.listingsService.addPhoto(this.listing.id, this.selectedNewPhoto).subscribe({
      next: (listing) => {
        this.listing = listing;
        this.selectedNewPhoto = null;
      },
      error: (error: HttpErrorResponse) => {
        this.addPhotoError = error.error?.message ?? 'Could not add photo.';
      },
    });
  }

  removePhoto(photoId: number): void {
    if (!this.listing) {
      return;
    }
    this.removePhotoError = '';
    this.listingsService.removePhoto(this.listing.id, photoId).subscribe({
      next: (listing) => {
        this.listing = listing;
      },
      error: (error: HttpErrorResponse) => {
        this.removePhotoError = error.error?.message ?? 'Could not remove photo.';
      },
    });
  }

  movePhoto(index: number, direction: -1 | 1): void {
    if (!this.listing) {
      return;
    }
    const targetIndex = index + direction;
    if (targetIndex < 0 || targetIndex >= this.listing.photos.length) {
      return;
    }
    const photoIds = this.listing.photos.map((photo) => photo.id);
    [photoIds[index], photoIds[targetIndex]] = [photoIds[targetIndex], photoIds[index]];

    this.reorderError = '';
    this.listingsService.reorderPhotos(this.listing.id, photoIds).subscribe({
      next: (listing) => {
        this.listing = listing;
      },
      error: (error: HttpErrorResponse) => {
        this.reorderError = error.error?.message ?? 'Could not reorder photos.';
      },
    });
  }
}
