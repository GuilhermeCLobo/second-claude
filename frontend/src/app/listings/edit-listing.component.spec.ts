import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';

import { EditListingComponent } from './edit-listing.component';
import { Listing } from './listing.model';

describe('EditListingComponent', () => {
  let fixture: ComponentFixture<EditListingComponent>;
  let httpMock: HttpTestingController;

  const existingListing: Listing = {
    id: 5,
    title: 'Camera',
    description: 'Digital camera',
    price: 200,
    category: 'ELECTRONICS',
    photos: [{ id: 1, reference: '/api/photos/camera.jpg' }],
    status: 'ACTIVE',
    ownerId: 1,
    buyerId: null,
    favorited: false,
  };

  function setUp(): void {
    TestBed.configureTestingModule({
      imports: [EditListingComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '5' } } } },
      ],
    });
    fixture = TestBed.createComponent(EditListingComponent);
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    const fetchReq = httpMock.expectOne((request) => request.url === '/api/listings/5' && request.method === 'GET');
    fetchReq.flush(existingListing);
    fixture.detectChanges();
  }

  afterEach(() => {
    httpMock.verify();
  });

  it('pre-fills the form from the fetched listing', () => {
    setUp();

    const element: HTMLElement = fixture.nativeElement;
    const title: HTMLInputElement = element.querySelector('input[name="title"]')!;
    const description: HTMLTextAreaElement = element.querySelector('textarea[name="description"]')!;
    const price: HTMLInputElement = element.querySelector('input[name="price"]')!;
    const category: HTMLSelectElement = element.querySelector('select[name="category"]')!;

    expect(title.value).toBe('Camera');
    expect(description.value).toBe('Digital camera');
    expect(price.value).toBe('200');
    expect(category.value).toBe('ELECTRONICS');
  });

  it('submits the full-replace update and navigates to the listing on success', () => {
    setUp();
    const router = TestBed.inject(Router);
    const navigateSpy = spyOn(router, 'navigate');

    const element: HTMLElement = fixture.nativeElement;
    const title: HTMLInputElement = element.querySelector('input[name="title"]')!;
    title.value = 'Vintage Camera';
    title.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const form: HTMLFormElement = element.querySelector('form')!;
    form.dispatchEvent(new Event('submit'));

    const req = httpMock.expectOne((request) => request.url === '/api/listings/5' && request.method === 'PUT');
    expect(req.request.body).toEqual({
      title: 'Vintage Camera',
      description: 'Digital camera',
      price: 200,
      category: 'ELECTRONICS',
    });

    req.flush({ ...existingListing, title: 'Vintage Camera' });

    expect(navigateSpy).toHaveBeenCalledWith(['/listings', 5]);
  });

  it('shows the server error message when the update fails', () => {
    setUp();

    const element: HTMLElement = fixture.nativeElement;
    const form: HTMLFormElement = element.querySelector('form')!;
    form.dispatchEvent(new Event('submit'));

    const req = httpMock.expectOne((request) => request.url === '/api/listings/5' && request.method === 'PUT');
    req.flush({ message: 'This listing is not ACTIVE' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();

    const error = fixture.nativeElement.querySelector('.error');
    expect(error?.textContent).toContain('This listing is not ACTIVE');
  });

  it('shows a not-found message when the listing cannot be fetched', () => {
    TestBed.configureTestingModule({
      imports: [EditListingComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '999' } } } },
      ],
    });
    fixture = TestBed.createComponent(EditListingComponent);
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    const fetchReq = httpMock.expectOne((request) => request.url === '/api/listings/999' && request.method === 'GET');
    fetchReq.flush({ message: 'No listing found with id 999' }, { status: 404, statusText: 'Not Found' });
    fixture.detectChanges();

    const notFound = fixture.nativeElement.querySelector('.not-found');
    expect(notFound?.textContent).toContain('Listing not found.');
  });
});
