import { Routes } from '@angular/router';

import { authGuard } from './auth/auth.guard';
import { LoginComponent } from './auth/login.component';
import { RegisterComponent } from './auth/register.component';
import { BrowseListingsComponent } from './listings/browse-listings.component';
import { CreateListingComponent } from './listings/create-listing.component';
import { ListingDetailComponent } from './listings/listing-detail.component';
import { MyListingsComponent } from './listings/my-listings.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'listings/new', component: CreateListingComponent, canActivate: [authGuard] },
  { path: 'my-listings', component: MyListingsComponent, canActivate: [authGuard] },
  { path: 'listings/:id', component: ListingDetailComponent },
  { path: '', component: BrowseListingsComponent },
];
