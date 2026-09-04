import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { CATEGORIES, Category } from './category';
import { Listing, ListingSort } from './listing.model';
import { ListingsService } from './listings.service';

const PAGE_SIZE = 12;

@Component({
  selector: 'app-browse-listings',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './browse-listings.component.html',
})
export class BrowseListingsComponent implements OnInit {
  readonly categories = CATEGORIES;
  listings: Listing[] = [];
  totalCount = 0;
  selectedCategory: Category | '' = '';
  search = '';
  minPrice: number | null = null;
  maxPrice: number | null = null;
  sort: ListingSort = 'NEWEST';
  page = 0;

  constructor(private readonly listingsService: ListingsService) {}

  ngOnInit(): void {
    this.load();
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.totalCount / PAGE_SIZE));
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  onCategoryChange(event: Event): void {
    this.selectedCategory = ((event.target as HTMLSelectElement).value as Category) || '';
    this.page = 0;
    this.load();
  }

  onSearchChange(event: Event): void {
    this.search = (event.target as HTMLInputElement).value;
    this.page = 0;
    this.load();
  }

  onMinPriceChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.minPrice = value === '' ? null : Number(value);
    this.page = 0;
    this.load();
  }

  onMaxPriceChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.maxPrice = value === '' ? null : Number(value);
    this.page = 0;
    this.load();
  }

  onSortChange(event: Event): void {
    this.sort = (event.target as HTMLSelectElement).value as ListingSort;
    this.page = 0;
    this.load();
  }

  goToPage(page: number): void {
    this.page = page;
    this.load();
  }

  private load(): void {
    this.listingsService
      .browse({
        category: this.selectedCategory || undefined,
        search: this.search || undefined,
        minPrice: this.minPrice ?? undefined,
        maxPrice: this.maxPrice ?? undefined,
        sort: this.sort,
        page: this.page,
        size: PAGE_SIZE,
      })
      .subscribe((result) => {
        this.listings = result.listings;
        this.totalCount = result.totalCount;
      });
  }
}
