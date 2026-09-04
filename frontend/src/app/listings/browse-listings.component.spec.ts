import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { BrowseListingsComponent } from './browse-listings.component';
import { Listing } from './listing.model';

describe('BrowseListingsComponent', () => {
  let fixture: ComponentFixture<BrowseListingsComponent>;
  let httpMock: HttpTestingController;

  const listings: Listing[] = [
    {
      id: 1,
      title: 'Bike',
      description: 'Road bike',
      price: 150,
      category: 'VEHICLES',
      photoReference: 'bike.jpg',
      status: 'ACTIVE',
      ownerId: 1,
      buyerId: null,
    },
    {
      id: 2,
      title: 'Sofa',
      description: 'Comfy sofa',
      price: 300,
      category: 'FURNITURE',
      photoReference: 'sofa.jpg',
      status: 'SOLD',
      ownerId: 1,
      buyerId: 2,
    },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [BrowseListingsComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    fixture = TestBed.createComponent(BrowseListingsComponent);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads and displays listings, marking SOLD ones distinctly', () => {
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings').flush(listings);
    fixture.detectChanges();

    const items: NodeListOf<HTMLLIElement> = fixture.nativeElement.querySelectorAll('li');
    expect(items.length).toBe(2);
    expect(items[1].classList).toContain('sold');
    expect(items[1].textContent).toContain('SOLD');
    expect(items[0].classList).not.toContain('sold');

    const thumbnails: NodeListOf<HTMLImageElement> = fixture.nativeElement.querySelectorAll('img.thumbnail');
    expect(thumbnails.length).toBe(2);
    expect(thumbnails[0].src).toContain('bike.jpg');
  });

  it('refetches with a category query param when the filter changes', () => {
    fixture.detectChanges();
    httpMock.expectOne((request) => request.url === '/api/listings').flush(listings);

    const select: HTMLSelectElement = fixture.nativeElement.querySelector('select');
    select.value = 'VEHICLES';
    select.dispatchEvent(new Event('change'));

    const req = httpMock.expectOne((request) => request.url === '/api/listings');
    expect(req.request.params.get('category')).toBe('VEHICLES');
    req.flush([listings[0]]);
  });
});
