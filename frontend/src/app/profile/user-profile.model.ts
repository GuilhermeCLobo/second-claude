import { Listing } from '../listings/listing.model';

export interface UserProfile {
  username: string;
  listings: Listing[];
}
