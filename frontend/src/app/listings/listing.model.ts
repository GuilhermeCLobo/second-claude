import { Category } from './category';

export type ListingStatus = 'ACTIVE' | 'SOLD';

export interface ListingPhoto {
  id: number;
  reference: string;
}

export interface Listing {
  id: number;
  title: string;
  description: string;
  price: number;
  category: Category;
  photos: ListingPhoto[];
  status: ListingStatus;
  ownerId: number;
  buyerId: number | null;
  favorited: boolean;
}

export type ListingSort = 'NEWEST' | 'PRICE_ASC' | 'PRICE_DESC';

export interface BrowseListingsParams {
  category?: Category;
  search?: string;
  minPrice?: number;
  maxPrice?: number;
  sort?: ListingSort;
  page?: number;
  size?: number;
}

export interface BrowseListingsResult {
  listings: Listing[];
  totalCount: number;
}
