import { Routes } from '@angular/router';

import { authGuard } from './auth/auth.guard';
import { ForgotPasswordComponent } from './auth/forgot-password.component';
import { LoginComponent } from './auth/login.component';
import { RegisterComponent } from './auth/register.component';
import { ResetPasswordComponent } from './auth/reset-password.component';
import { BrowseListingsComponent } from './listings/browse-listings.component';
import { CreateListingComponent } from './listings/create-listing.component';
import { EditListingComponent } from './listings/edit-listing.component';
import { ListingDetailComponent } from './listings/listing-detail.component';
import { MyFavoritesComponent } from './listings/my-favorites.component';
import { MyListingsComponent } from './listings/my-listings.component';
import { UserProfileComponent } from './profile/user-profile.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'listings/new', component: CreateListingComponent, canActivate: [authGuard] },
  { path: 'listings/:id/edit', component: EditListingComponent, canActivate: [authGuard] },
  { path: 'my-listings', component: MyListingsComponent, canActivate: [authGuard] },
  { path: 'my-favorites', component: MyFavoritesComponent, canActivate: [authGuard] },
  { path: 'users/:username', component: UserProfileComponent },
  { path: 'listings/:id', component: ListingDetailComponent },
  { path: '', component: BrowseListingsComponent },
];
