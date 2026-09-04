import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Listing } from './listing.model';
import { ListingsService } from './listings.service';

@Component({
  selector: 'app-my-favorites',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './my-favorites.component.html',
})
export class MyFavoritesComponent implements OnInit {
  favoritedListings: Listing[] = [];
  loading = true;

  constructor(private readonly listingsService: ListingsService) {}

  ngOnInit(): void {
    this.listingsService.myFavorites().subscribe((listings) => {
      this.favoritedListings = listings;
      this.loading = false;
    });
  }

  unfavorite(listing: Listing): void {
    this.listingsService.unfavorite(listing.id).subscribe(() => {
      this.favoritedListings = this.favoritedListings.filter((l) => l.id !== listing.id);
    });
  }
}
