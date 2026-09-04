import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ActivatedRoute, convertToParamMap } from '@angular/router';

import { ListingDetailComponent } from './listing-detail.component';
import { Listing } from './listing.model';

describe('ListingDetailComponent', () => {
  let fixture: ComponentFixture<ListingDetailComponent>;
  let httpMock: HttpTestingController;

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

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ListingDetailComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '1' }) } },
        },
      ],
    });
    fixture = TestBed.createComponent(ListingDetailComponent);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
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
});
