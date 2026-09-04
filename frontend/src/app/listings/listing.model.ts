import { Category } from './category';

export type ListingStatus = 'ACTIVE' | 'SOLD';

export interface Listing {
  id: number;
  title: string;
  description: string;
  price: number;
  category: Category;
  photoReference: string | null;
  status: ListingStatus;
  ownerId: number;
  buyerId: number | null;
}
