import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { CATEGORIES, Category } from './category';
import { Listing } from './listing.model';
import { ListingsService } from './listings.service';

@Component({
  selector: 'app-browse-listings',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './browse-listings.component.html',
})
export class BrowseListingsComponent implements OnInit {
  readonly categories = CATEGORIES;
  listings: Listing[] = [];
  selectedCategory: Category | '' = '';

  constructor(private readonly listingsService: ListingsService) {}

  ngOnInit(): void {
    this.load();
  }

  onCategoryChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedCategory = (value as Category) || '';
    this.load();
  }

  private load(): void {
    this.listingsService.browse(this.selectedCategory || undefined).subscribe((listings) => {
      this.listings = listings;
    });
  }
}
