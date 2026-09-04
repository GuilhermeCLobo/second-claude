import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';

import { CreateListingComponent } from './create-listing.component';
import { Listing } from './listing.model';

describe('CreateListingComponent', () => {
  let fixture: ComponentFixture<CreateListingComponent>;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [CreateListingComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    fixture = TestBed.createComponent(CreateListingComponent);
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  function setPhoto(): File {
    const element: HTMLElement = fixture.nativeElement;
    const photoInput: HTMLInputElement = element.querySelector('input[name="photo"]')!;
    const photo = new File(['fake-photo-bytes'], 'camera.jpg', { type: 'image/jpeg' });
    Object.defineProperty(photoInput, 'files', { value: [photo] });
    photoInput.dispatchEvent(new Event('change'));
    return photo;
  }

  function fillAndSubmit(includePhoto = true): void {
    const element: HTMLElement = fixture.nativeElement;
    const title: HTMLInputElement = element.querySelector('input[name="title"]')!;
    const description: HTMLTextAreaElement = element.querySelector('textarea[name="description"]')!;
    const price: HTMLInputElement = element.querySelector('input[name="price"]')!;
    const category: HTMLSelectElement = element.querySelector('select[name="category"]')!;

    title.value = 'Camera';
    title.dispatchEvent(new Event('input'));
    description.value = 'Digital camera';
    description.dispatchEvent(new Event('input'));
    price.value = '200';
    price.dispatchEvent(new Event('input'));
    category.value = 'ELECTRONICS';
    category.dispatchEvent(new Event('change'));
    if (includePhoto) {
      setPhoto();
    }
    fixture.detectChanges();

    const form: HTMLFormElement = element.querySelector('form')!;
    form.dispatchEvent(new Event('submit'));
  }

  it('submits the form and navigates to the new listing on success', () => {
    const router = TestBed.inject(Router);
    const navigateSpy = spyOn(router, 'navigate');

    fillAndSubmit();

    const req = httpMock.expectOne((request) => request.url === '/api/listings' && request.method === 'POST');
    expect(req.request.body instanceof FormData).toBeTrue();

    const created: Listing = {
      id: 5,
      title: 'Camera',
      description: 'Digital camera',
      price: 200,
      category: 'ELECTRONICS',
      photoReference: '/api/photos/camera.jpg',
      status: 'ACTIVE',
      ownerId: 1,
      buyerId: null,
    };
    req.flush(created);

    expect(navigateSpy).toHaveBeenCalledWith(['/listings', 5]);
  });

  it('shows the server error message when creation fails', () => {
    fillAndSubmit();

    const req = httpMock.expectOne((request) => request.url === '/api/listings' && request.method === 'POST');
    req.flush({ message: 'Authentication required' }, { status: 401, statusText: 'Unauthorized' });
    fixture.detectChanges();

    const error = fixture.nativeElement.querySelector('.error');
    expect(error?.textContent).toContain('Authentication required');
  });

  it('disables submit until required fields are filled', () => {
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button[type="submit"]');
    expect(button.disabled).toBeTrue();
  });

  it('keeps submit disabled when fields are filled but no photo is selected', () => {
    fillAndSubmit(false);

    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button[type="submit"]');
    expect(button.disabled).toBeTrue();
    httpMock.expectNone((request) => request.url === '/api/listings');
  });
});
