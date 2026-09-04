import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';

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
    photoReference: 'bike.jpg',
    status: 'ACTIVE',
    ownerId: 1,
    buyerId: null,
  };

  function setUp(): void {
    router = jasmine.createSpyObj<Router>('Router', ['navigateByUrl']);
    TestBed.configureTestingModule({
      imports: [ListingDetailComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Router, useValue: router },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '1' }) } },
        },
      ],
    });
    fixture = TestBed.createComponent(ListingDetailComponent);
    httpMock = TestBed.inject(HttpTestingController);
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
});
