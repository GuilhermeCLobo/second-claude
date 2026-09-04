import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '../auth/auth.service';
import { CATEGORIES } from './category';
import { Listing } from './listing.model';
import { ListingsService } from './listings.service';

@Component({
  selector: 'app-listing-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './listing-detail.component.html',
})
export class ListingDetailComponent implements OnInit {
  readonly categories = CATEGORIES;
  listing: Listing | null = null;
  notFound = false;
  deleteError = '';
  buyError = '';

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
}
