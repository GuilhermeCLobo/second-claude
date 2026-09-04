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
}
