export type Category =
  | 'ELECTRONICS'
  | 'FURNITURE'
  | 'CLOTHING_AND_ACCESSORIES'
  | 'BOOKS_AND_MEDIA'
  | 'HOME_AND_GARDEN'
  | 'VEHICLES'
  | 'OTHER';

export const CATEGORIES: { value: Category; label: string }[] = [
  { value: 'ELECTRONICS', label: 'Electronics' },
  { value: 'FURNITURE', label: 'Furniture' },
  { value: 'CLOTHING_AND_ACCESSORIES', label: 'Clothing & Accessories' },
  { value: 'BOOKS_AND_MEDIA', label: 'Books & Media' },
  { value: 'HOME_AND_GARDEN', label: 'Home & Garden' },
  { value: 'VEHICLES', label: 'Vehicles' },
  { value: 'OTHER', label: 'Other' },
];
