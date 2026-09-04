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

  it('fetches all listings with no query params when no filters are given', () => {
    let result: { listings: Listing[]; totalCount: number } | undefined;

    service.browse().subscribe((response) => {
      result = response;
    });

    const req = httpMock.expectOne((request) => request.url === '/api/listings');
    expect(req.request.params.has('category')).toBeFalse();
    expect(req.request.params.has('search')).toBeFalse();
    expect(req.request.params.has('minPrice')).toBeFalse();
    expect(req.request.params.has('maxPrice')).toBeFalse();
    expect(req.request.params.has('sort')).toBeFalse();
    expect(req.request.params.has('page')).toBeFalse();
    expect(req.request.params.has('size')).toBeFalse();

    req.flush({ listings: [], totalCount: 0 });

    expect(result).toEqual({ listings: [], totalCount: 0 });
  });

  it('sends the category as a query parameter when filtering', () => {
    service.browse({ category: 'ELECTRONICS' }).subscribe();

    const req = httpMock.expectOne((request) => request.url === '/api/listings');
    expect(req.request.params.get('category')).toBe('ELECTRONICS');

    req.flush({ listings: [], totalCount: 0 });
  });

  it('sends search, price range, sort, and pagination as query parameters', () => {
    service
      .browse({ search: 'camera', minPrice: 10, maxPrice: 100, sort: 'PRICE_ASC', page: 2, size: 24 })
      .subscribe();

    const req = httpMock.expectOne((request) => request.url === '/api/listings');
    expect(req.request.params.get('search')).toBe('camera');
    expect(req.request.params.get('minPrice')).toBe('10');
    expect(req.request.params.get('maxPrice')).toBe('100');
    expect(req.request.params.get('sort')).toBe('PRICE_ASC');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('24');

    req.flush({ listings: [], totalCount: 0 });
  });

  it('posts a new listing as multipart form data with the listing fields and the photo', (done) => {
    let result: Listing | undefined;
    const listing: Listing = {
      id: 3,
      title: 'Camera',
      description: 'Digital camera',
      price: 200,
      category: 'ELECTRONICS',
      photos: [{ id: 1, reference: '/api/photos/camera.jpg' }],
      status: 'ACTIVE',
      ownerId: 1,
      buyerId: null,
    };
    const photo = new File(['fake-photo-bytes'], 'camera.jpg', { type: 'image/jpeg' });

    service
      .create({ title: 'Camera', description: 'Digital camera', price: 200, category: 'ELECTRONICS' }, photo)
      .subscribe((response) => {
        result = response;
      });

    const req = httpMock.expectOne((request) => request.url === '/api/listings' && request.method === 'POST');
    const formData = req.request.body as FormData;
    expect(formData.get('photo')).toBe(photo);

    (formData.get('listing') as Blob).text().then((listingJson) => {
      expect(JSON.parse(listingJson)).toEqual({
        title: 'Camera',
        description: 'Digital camera',
        price: 200,
        category: 'ELECTRONICS',
      });
      done();
    });

    req.flush(listing);
    expect(result).toEqual(listing);
  });

  it('fetches a single listing by id', () => {
    let result: Listing | undefined;
    const listing: Listing = {
      id: 1,
      title: 'Bike',
      description: 'Road bike',
      price: 150,
      category: 'VEHICLES',
      photos: [{ id: 1, reference: 'bike.jpg' }],
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

  it('sends a DELETE request for the given listing id', () => {
    let completed = false;

    service.delete(1).subscribe(() => {
      completed = true;
    });

    const req = httpMock.expectOne((request) => request.url === '/api/listings/1' && request.method === 'DELETE');
    req.flush(null);

    expect(completed).toBeTrue();
  });

  it('posts to the buy endpoint for the given listing id', () => {
    let result: Listing | undefined;
    const listing: Listing = {
      id: 1,
      title: 'Bike',
      description: 'Road bike',
      price: 150,
      category: 'VEHICLES',
      photos: [{ id: 1, reference: 'bike.jpg' }],
      status: 'SOLD',
      ownerId: 2,
      buyerId: 1,
    };

    service.buy(1).subscribe((response) => {
      result = response;
    });

    const req = httpMock.expectOne((request) => request.url === '/api/listings/1/buy' && request.method === 'POST');
    req.flush(listing);

    expect(result).toEqual(listing);
  });

  it('posts a new photo as multipart form data to the add-photo endpoint', () => {
    let result: Listing | undefined;
    const listing: Listing = {
      id: 1,
      title: 'Bike',
      description: 'Road bike',
      price: 150,
      category: 'VEHICLES',
      photos: [{ id: 1, reference: 'bike.jpg' }, { id: 2, reference: 'bike-2.jpg' }],
      status: 'ACTIVE',
      ownerId: 1,
      buyerId: null,
    };
    const photo = new File(['fake-photo-bytes'], 'bike-2.jpg', { type: 'image/jpeg' });

    service.addPhoto(1, photo).subscribe((response) => {
      result = response;
    });

    const req = httpMock.expectOne((request) => request.url === '/api/listings/1/photos' && request.method === 'POST');
    const formData = req.request.body as FormData;
    expect(formData.get('photo')).toBe(photo);
    req.flush(listing);

    expect(result).toEqual(listing);
  });

  it('sends a DELETE request to the remove-photo endpoint for the given photo id', () => {
    let result: Listing | undefined;
    const listing: Listing = {
      id: 1,
      title: 'Bike',
      description: 'Road bike',
      price: 150,
      category: 'VEHICLES',
      photos: [{ id: 1, reference: 'bike.jpg' }],
      status: 'ACTIVE',
      ownerId: 1,
      buyerId: null,
    };

    service.removePhoto(1, 2).subscribe((response) => {
      result = response;
    });

    const req = httpMock.expectOne(
      (request) => request.url === '/api/listings/1/photos/2' && request.method === 'DELETE',
    );
    req.flush(listing);

    expect(result).toEqual(listing);
  });

  it('sends the ordered photo ids to the reorder endpoint', () => {
    let result: Listing | undefined;
    const listing: Listing = {
      id: 1,
      title: 'Bike',
      description: 'Road bike',
      price: 150,
      category: 'VEHICLES',
      photos: [{ id: 2, reference: 'bike-2.jpg' }, { id: 1, reference: 'bike.jpg' }],
      status: 'ACTIVE',
      ownerId: 1,
      buyerId: null,
    };

    service.reorderPhotos(1, [2, 1]).subscribe((response) => {
      result = response;
    });

    const req = httpMock.expectOne(
      (request) => request.url === '/api/listings/1/photos/order' && request.method === 'PUT',
    );
    expect(req.request.body).toEqual({ photoIds: [2, 1] });
    req.flush(listing);

    expect(result).toEqual(listing);
  });
});
