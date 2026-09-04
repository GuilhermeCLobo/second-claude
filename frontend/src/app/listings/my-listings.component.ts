import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Listing } from './listing.model';
import { ListingsService } from './listings.service';

@Component({
  selector: 'app-my-listings',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './my-listings.component.html',
})
export class MyListingsComponent implements OnInit {
  postedListings: Listing[] = [];
  boughtListings: Listing[] = [];
  postedLoading = true;
  boughtLoading = true;

  constructor(private readonly listingsService: ListingsService) {}

  ngOnInit(): void {
    this.listingsService.myPosted().subscribe((listings) => {
      this.postedListings = listings;
      this.postedLoading = false;
    });
    this.listingsService.myBought().subscribe((listings) => {
      this.boughtListings = listings;
      this.boughtLoading = false;
    });
  }
}
