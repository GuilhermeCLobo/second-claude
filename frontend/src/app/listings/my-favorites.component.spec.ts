import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { Listing } from './listing.model';
import { MyFavoritesComponent } from './my-favorites.component';

describe('MyFavoritesComponent', () => {
  let fixture: ComponentFixture<MyFavoritesComponent>;
  let httpMock: HttpTestingController;

  const favoritedListing: Listing = {
    id: 1,
    title: 'Bike',
    description: 'Road bike',
    price: 150,
    category: 'VEHICLES',
    photos: [{ id: 1, reference: 'bike.jpg' }],
    status: 'ACTIVE',
    ownerId: 2,
    buyerId: null,
    favorited: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MyFavoritesComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    fixture = TestBed.createComponent(MyFavoritesComponent);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('fetches and displays the current user\'s favorited listings', () => {
    fixture.detectChanges();

    httpMock.expectOne((request) => request.url === '/api/listings/mine/favorites').flush([favoritedListing]);
    fixture.detectChanges();

    const section = fixture.nativeElement.querySelector('.favorited-listings');
    expect(section.textContent).toContain('Bike');
  });

  it('shows an empty message when there are no favorited listings', () => {
    fixture.detectChanges();

    httpMock.expectOne((request) => request.url === '/api/listings/mine/favorites').flush([]);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.empty')).toBeTruthy();
  });

  it('unfavoriting a listing removes it from the list', () => {
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings/mine/favorites').flush([favoritedListing]);
    fixture.detectChanges();

    const button: HTMLButtonElement = fixture.nativeElement.querySelector('.unfavorite');
    button.click();

    httpMock
      .expectOne((request) => request.url === '/api/listings/1/favorite' && request.method === 'DELETE')
      .flush(null);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.empty')).toBeTruthy();
  });
});
