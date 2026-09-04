import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { Listing } from './listing.model';
import { ListingsService } from './listings.service';

describe('ListingsService', () => {
  let service: ListingsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ListingsService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ListingsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('fetches all listings when no category is given', () => {
    let result: Listing[] | undefined;

    service.browse().subscribe((listings) => {
      result = listings;
    });

    const req = httpMock.expectOne((request) => request.url === '/api/listings');
    expect(req.request.params.has('category')).toBeFalse();

    req.flush([]);

    expect(result).toEqual([]);
  });

  it('sends the category as a query parameter when filtering', () => {
    service.browse('ELECTRONICS').subscribe();

    const req = httpMock.expectOne((request) => request.url === '/api/listings');
    expect(req.request.params.get('category')).toBe('ELECTRONICS');

    req.flush([]);
  });

  it('fetches a single listing by id', () => {
    let result: Listing | undefined;
    const listing: Listing = {
      id: 1,
      title: 'Bike',
      description: 'Road bike',
      price: 150,
      category: 'VEHICLES',
      photoReference: 'bike.jpg',
      status: 'ACTIVE',
      ownerId: 1,
      buyerId: null,
    };

    service.getById(1).subscribe((response) => {
      result = response;
    });

    const req = httpMock.expectOne((request) => request.url === '/api/listings/1');
    req.flush(listing);

    expect(result).toEqual(listing);
  });
});
