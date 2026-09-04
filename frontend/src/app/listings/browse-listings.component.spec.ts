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
      photos: [{ id: 1, reference: 'bike.jpg' }],
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
      photos: [{ id: 2, reference: 'sofa.jpg' }],
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
    httpMock
      .expectOne((request) => request.url === '/api/listings')
      .flush({ listings, totalCount: 2 });
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
    httpMock
      .expectOne((request) => request.url === '/api/listings')
      .flush({ listings, totalCount: 2 });

    const select: HTMLSelectElement = fixture.nativeElement.querySelector('select');
    select.value = 'VEHICLES';
    select.dispatchEvent(new Event('change'));

    const req = httpMock.expectOne((request) => request.url === '/api/listings');
    expect(req.request.params.get('category')).toBe('VEHICLES');
    req.flush({ listings: [listings[0]], totalCount: 1 });
  });

  it('refetches with a search query param when the search input changes', () => {
    fixture.detectChanges();
    httpMock
      .expectOne((request) => request.url === '/api/listings')
      .flush({ listings, totalCount: 2 });

    const input: HTMLInputElement = fixture.nativeElement.querySelector('input[type="text"]');
    input.value = 'bike';
    input.dispatchEvent(new Event('change'));

    const req = httpMock.expectOne((request) => request.url === '/api/listings');
    expect(req.request.params.get('search')).toBe('bike');
    req.flush({ listings: [listings[0]], totalCount: 1 });
  });

  it('refetches with min and max price query params when the price inputs change', () => {
    fixture.detectChanges();
    httpMock
      .expectOne((request) => request.url === '/api/listings')
      .flush({ listings, totalCount: 2 });

    const priceInputs: NodeListOf<HTMLInputElement> = fixture.nativeElement.querySelectorAll('input[type="number"]');
    priceInputs[0].value = '50';
    priceInputs[0].dispatchEvent(new Event('change'));

    let req = httpMock.expectOne((request) => request.url === '/api/listings');
    expect(req.request.params.get('minPrice')).toBe('50');
    req.flush({ listings, totalCount: 2 });

    priceInputs[1].value = '200';
    priceInputs[1].dispatchEvent(new Event('change'));

    req = httpMock.expectOne((request) => request.url === '/api/listings');
    expect(req.request.params.get('maxPrice')).toBe('200');
    req.flush({ listings, totalCount: 2 });
  });

  it('refetches with a sort query param when the sort control changes', () => {
    fixture.detectChanges();
    httpMock
      .expectOne((request) => request.url === '/api/listings')
      .flush({ listings, totalCount: 2 });

    const selects: NodeListOf<HTMLSelectElement> = fixture.nativeElement.querySelectorAll('select');
    const sortSelect = selects[1];
    sortSelect.value = 'PRICE_ASC';
    sortSelect.dispatchEvent(new Event('change'));

    const req = httpMock.expectOne((request) => request.url === '/api/listings');
    expect(req.request.params.get('sort')).toBe('PRICE_ASC');
    req.flush({ listings, totalCount: 2 });
  });

  it('requests the right page when a pagination control is clicked', () => {
    fixture.detectChanges();
    httpMock
      .expectOne((request) => request.url === '/api/listings')
      .flush({ listings, totalCount: 30 });
    fixture.detectChanges();

    const pageButtons: NodeListOf<HTMLButtonElement> = fixture.nativeElement.querySelectorAll('nav.pagination button');
    expect(pageButtons.length).toBe(3);
    pageButtons[1].click();

    const req = httpMock.expectOne((request) => request.url === '/api/listings');
    expect(req.request.params.get('page')).toBe('1');
    req.flush({ listings, totalCount: 30 });
  });
});
