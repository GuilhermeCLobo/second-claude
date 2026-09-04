import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';

import { AUTH_SESSION_STORAGE_KEY } from '../auth/auth.service';
import { ListingDetailComponent } from './listing-detail.component';
import { Listing } from './listing.model';

describe('ListingDetailComponent', () => {
  let fixture: ComponentFixture<ListingDetailComponent>;
  let httpMock: HttpTestingController;
  let router: jasmine.SpyObj<Router>;

  const listing: Listing = {
    id: 1,
    title: 'Bike',
    description: 'Road bike',
    price: 150,
    category: 'VEHICLES',
    photos: [{ id: 1, reference: 'bike.jpg' }],
    status: 'ACTIVE',
    ownerId: 1,
    ownerUsername: 'owner',
    buyerId: null,
    favorited: false,
  };

  function setUp(): void {
    TestBed.configureTestingModule({
      imports: [ListingDetailComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '1' }) } },
        },
      ],
    });
    fixture = TestBed.createComponent(ListingDetailComponent);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router) as unknown as jasmine.SpyObj<Router>;
    spyOn(router, 'navigate');
    spyOn(router, 'navigateByUrl');
  }

  function setUpAsLoggedInUser(userId: number): void {
    localStorage.setItem(
      AUTH_SESSION_STORAGE_KEY,
      JSON.stringify({ token: 'jwt-token', userId, username: 'owner' }),
    );
    TestBed.resetTestingModule();
    setUp();
  }

  beforeEach(() => {
    localStorage.clear();
    setUp();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('displays all fields of the listing, including photo and status', () => {
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(listing);
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    expect(element.querySelector('h1')?.textContent).toContain('Bike');
    expect(element.querySelector('.description')?.textContent).toContain('Road bike');
    expect(element.querySelector('.status')?.textContent).toContain('ACTIVE');
    expect(element.querySelector('img')?.getAttribute('src')).toBe('bike.jpg');
  });

  it("links to the owner's profile", () => {
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(listing);
    fixture.detectChanges();

    const link: HTMLAnchorElement = fixture.nativeElement.querySelector('.owner-link');
    expect(link.textContent).toContain('owner');
    expect(link.getAttribute('href')).toBe('/users/owner');
  });

  it('shows a not-found message when the listing does not exist', () => {
    fixture.detectChanges();
    httpMock
      .expectOne((request) => request.url === '/api/listings/1')
      .flush({ message: 'No listing found with id 1' }, { status: 404, statusText: 'Not Found' });
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    expect(element.querySelector('.not-found')?.textContent).toContain('not found');
  });

  it('offers neither a delete nor a buy action when no one is logged in', () => {
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(listing);
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    expect(element.querySelector('.delete-listing')).toBeNull();
    expect(element.querySelector('.buy-listing')).toBeNull();
    expect(element.querySelector('.favorite-toggle')).toBeNull();
  });

  it('offers a favorite toggle to a logged-in user, reflecting server state and toggling on click', () => {
    setUpAsLoggedInUser(2);
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(listing);
    fixture.detectChanges();

    const button: HTMLButtonElement = fixture.nativeElement.querySelector('.favorite-toggle');
    expect(button).toBeTruthy();
    expect(button.textContent).toContain('Favorite');
    button.click();

    httpMock
      .expectOne((request) => request.url === '/api/listings/1/favorite' && request.method === 'POST')
      .flush({ ...listing, favorited: true });
    fixture.detectChanges();

    expect(button.textContent).toContain('Unfavorite');
    button.click();

    httpMock
      .expectOne((request) => request.url === '/api/listings/1/favorite' && request.method === 'DELETE')
      .flush(null);
    fixture.detectChanges();

    expect(button.textContent).toContain('Favorite');
  });

  it('a User can favorite their own listing', () => {
    setUpAsLoggedInUser(1);
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(listing);
    fixture.detectChanges();

    const button: HTMLButtonElement = fixture.nativeElement.querySelector('.favorite-toggle');
    expect(button).toBeTruthy();
  });

  it('offers a buy action, but not a delete action, to a User who does not own the listing', () => {
    setUpAsLoggedInUser(2);
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(listing);
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    expect(element.querySelector('.delete-listing')).toBeNull();
    expect(element.querySelector('.buy-listing')).toBeTruthy();
  });

  it('offers neither action for a SOLD listing, even to its owner', () => {
    setUpAsLoggedInUser(1);
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush({ ...listing, status: 'SOLD' });
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    expect(element.querySelector('.delete-listing')).toBeNull();
    expect(element.querySelector('.buy-listing')).toBeNull();
  });

  it('offers an edit action to the owner of an ACTIVE listing, and it navigates to the edit form', () => {
    setUpAsLoggedInUser(1);
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(listing);
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    const button = element.querySelector('.edit-listing') as HTMLButtonElement;
    expect(button).toBeTruthy();
    button.click();

    expect(router.navigate).toHaveBeenCalledWith(['/listings', 1, 'edit']);
  });

  it('does not offer an edit action to a non-owner or for a SOLD listing', () => {
    setUpAsLoggedInUser(2);
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(listing);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.edit-listing')).toBeNull();
  });

  it('offers a delete action, but not a buy action, to the owner of an ACTIVE listing, and deleting navigates back to browse', () => {
    setUpAsLoggedInUser(1);
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(listing);
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    expect(element.querySelector('.buy-listing')).toBeNull();
    const button = element.querySelector('.delete-listing') as HTMLButtonElement;
    expect(button).toBeTruthy();
    button.click();

    httpMock.expectOne((request) => request.url === '/api/listings/1' && request.method === 'DELETE').flush(null);

    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
  });

  it('shows an error and keeps the listing when deleting fails', () => {
    setUpAsLoggedInUser(1);
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(listing);
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    const button = element.querySelector('.delete-listing') as HTMLButtonElement;
    button.click();

    httpMock
      .expectOne((request) => request.url === '/api/listings/1' && request.method === 'DELETE')
      .flush({ message: 'This listing is not ACTIVE' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();

    expect(router.navigateByUrl).not.toHaveBeenCalled();
    expect(element.querySelector('.delete-error')?.textContent).toContain('Could not delete');
  });

  it('buying updates the listing in place to reflect the new SOLD status', () => {
    setUpAsLoggedInUser(2);
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(listing);
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    const button = element.querySelector('.buy-listing') as HTMLButtonElement;
    expect(button).toBeTruthy();
    button.click();

    httpMock
      .expectOne((request) => request.url === '/api/listings/1/buy' && request.method === 'POST')
      .flush({ ...listing, status: 'SOLD', buyerId: 2 });
    fixture.detectChanges();

    expect(element.querySelector('.status')?.textContent).toContain('SOLD');
    expect(element.querySelector('.buy-listing')).toBeNull();
  });

  it('shows an error and keeps the listing when buying fails', () => {
    setUpAsLoggedInUser(2);
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(listing);
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    const button = element.querySelector('.buy-listing') as HTMLButtonElement;
    button.click();

    httpMock
      .expectOne((request) => request.url === '/api/listings/1/buy' && request.method === 'POST')
      .flush({ message: 'This listing is not ACTIVE' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();

    expect(element.querySelector('.buy-error')?.textContent).toContain('Could not buy');
  });

  it('shows every photo in order in the gallery', () => {
    const twoPhotoListing: Listing = {
      ...listing,
      photos: [
        { id: 1, reference: 'bike.jpg' },
        { id: 2, reference: 'bike-2.jpg' },
      ],
    };
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(twoPhotoListing);
    fixture.detectChanges();

    const images: NodeListOf<HTMLImageElement> = fixture.nativeElement.querySelectorAll('.photos img');
    expect(images.length).toBe(2);
    expect(images[0].getAttribute('src')).toBe('bike.jpg');
    expect(images[1].getAttribute('src')).toBe('bike-2.jpg');
  });

  it('offers photo management to the owner of an ACTIVE listing, but not to a non-owner', () => {
    setUpAsLoggedInUser(2);
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(listing);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.manage-photos')).toBeNull();
  });

  it('disables removing a photo when it is the only one remaining', () => {
    setUpAsLoggedInUser(1);
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(listing);
    fixture.detectChanges();

    const button: HTMLButtonElement = fixture.nativeElement.querySelector('.remove-photo');
    expect(button.disabled).toBeTrue();
  });

  it('adding a photo posts it and refreshes the listing with the new photo appended', () => {
    setUpAsLoggedInUser(1);
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(listing);
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    const photoInput: HTMLInputElement = element.querySelector('input[name="newPhoto"]')!;
    const photo = new File(['fake-photo-bytes'], 'bike-2.jpg', { type: 'image/jpeg' });
    Object.defineProperty(photoInput, 'files', { value: [photo] });
    photoInput.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    const addButton: HTMLButtonElement = element.querySelector('.add-photo')!;
    expect(addButton.disabled).toBeFalse();
    addButton.click();

    const req = httpMock.expectOne(
      (request) => request.url === '/api/listings/1/photos' && request.method === 'POST',
    );
    expect((req.request.body as FormData).get('photo')).toBe(photo);
    req.flush({
      ...listing,
      photos: [...listing.photos, { id: 2, reference: 'bike-2.jpg' }],
    });
    fixture.detectChanges();

    expect(element.querySelectorAll('.photos img').length).toBe(2);
  });

  it('removing a photo deletes it and refreshes the listing', () => {
    const twoPhotoListing: Listing = {
      ...listing,
      photos: [
        { id: 1, reference: 'bike.jpg' },
        { id: 2, reference: 'bike-2.jpg' },
      ],
    };
    setUpAsLoggedInUser(1);
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(twoPhotoListing);
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    const removeButtons: NodeListOf<HTMLButtonElement> = element.querySelectorAll('.remove-photo');
    expect(removeButtons[0].disabled).toBeFalse();
    removeButtons[0].click();

    httpMock
      .expectOne((request) => request.url === '/api/listings/1/photos/1' && request.method === 'DELETE')
      .flush({ ...twoPhotoListing, photos: [{ id: 2, reference: 'bike-2.jpg' }] });
    fixture.detectChanges();

    expect(element.querySelectorAll('.photos img').length).toBe(1);
  });

  it('moving a photo up sends the reordered photo ids and refreshes the listing', () => {
    const twoPhotoListing: Listing = {
      ...listing,
      photos: [
        { id: 1, reference: 'bike.jpg' },
        { id: 2, reference: 'bike-2.jpg' },
      ],
    };
    setUpAsLoggedInUser(1);
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/1').flush(twoPhotoListing);
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    const moveUpButtons: NodeListOf<HTMLButtonElement> = element.querySelectorAll('.move-photo-up');
    moveUpButtons[1].click();

    const req = httpMock.expectOne(
      (request) => request.url === '/api/listings/1/photos/order' && request.method === 'PUT',
    );
    expect(req.request.body).toEqual({ photoIds: [2, 1] });
    req.flush({
      ...twoPhotoListing,
      photos: [
        { id: 2, reference: 'bike-2.jpg' },
        { id: 1, reference: 'bike.jpg' },
      ],
    });
    fixture.detectChanges();

    const images: NodeListOf<HTMLImageElement> = element.querySelectorAll('.photos img');
    expect(images[0].getAttribute('src')).toBe('bike-2.jpg');
  });
});
