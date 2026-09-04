import { Routes } from '@angular/router';

import { LoginComponent } from './auth/login.component';
import { RegisterComponent } from './auth/register.component';
import { BrowseListingsComponent } from './listings/browse-listings.component';
import { ListingDetailComponent } from './listings/listing-detail.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'listings/:id', component: ListingDetailComponent },
  { path: '', component: BrowseListingsComponent },
];
