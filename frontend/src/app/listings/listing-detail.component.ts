import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

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

  constructor(
    private readonly route: ActivatedRoute,
    private readonly listingsService: ListingsService,
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
}
