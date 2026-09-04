import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { Listing } from './listing.model';
import { MyListingsComponent } from './my-listings.component';

describe('MyListingsComponent', () => {
  let fixture: ComponentFixture<MyListingsComponent>;
  let httpMock: HttpTestingController;

  const postedListing: Listing = {
    id: 1,
    title: 'Bike',
    description: 'Road bike',
    price: 150,
    category: 'VEHICLES',
    photos: [{ id: 1, reference: 'bike.jpg' }],
    status: 'ACTIVE',
    ownerId: 1,
    buyerId: null,
    favorited: false,
  };

  const boughtListing: Listing = {
    id: 2,
    title: 'Sofa',
    description: 'Comfy sofa',
    price: 300,
    category: 'FURNITURE',
    photos: [{ id: 2, reference: 'sofa.jpg' }],
    status: 'SOLD',
    ownerId: 2,
    buyerId: 1,
    favorited: false,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MyListingsComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    fixture = TestBed.createComponent(MyListingsComponent);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('fetches and displays both posted and bought listings, distinguishing them', () => {
    fixture.detectChanges();

    httpMock.expectOne((request) => request.url === '/api/listings/mine/posted').flush([postedListing]);
    httpMock.expectOne((request) => request.url === '/api/listings/mine/bought').flush([boughtListing]);
    fixture.detectChanges();

    const posted = fixture.nativeElement.querySelector('.posted-listings');
    const bought = fixture.nativeElement.querySelector('.bought-listings');
    expect(posted.textContent).toContain('Bike');
    expect(posted.textContent).not.toContain('Sofa');
    expect(bought.textContent).toContain('Sofa');
    expect(bought.textContent).not.toContain('Bike');
  });

  it('shows an empty message for each section when there are no listings', () => {
    fixture.detectChanges();

    httpMock.expectOne((request) => request.url === '/api/listings/mine/posted').flush([]);
    httpMock.expectOne((request) => request.url === '/api/listings/mine/bought').flush([]);
    fixture.detectChanges();

    const posted = fixture.nativeElement.querySelector('.posted-listings');
    const bought = fixture.nativeElement.querySelector('.bought-listings');
    expect(posted.querySelector('.empty')).toBeTruthy();
    expect(bought.querySelector('.empty')).toBeTruthy();
  });
});
