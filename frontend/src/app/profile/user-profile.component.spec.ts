import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ActivatedRoute, convertToParamMap } from '@angular/router';

import { Listing } from '../listings/listing.model';
import { UserProfile } from './user-profile.model';
import { UserProfileComponent } from './user-profile.component';

describe('UserProfileComponent', () => {
  let fixture: ComponentFixture<UserProfileComponent>;
  let httpMock: HttpTestingController;

  const listing: Listing = {
    id: 1,
    title: 'Bike',
    description: 'Road bike',
    price: 150,
    category: 'VEHICLES',
    photos: [{ id: 1, reference: 'bike.jpg' }],
    status: 'ACTIVE',
    ownerId: 1,
    ownerUsername: 'bike-seller',
    buyerId: null,
    favorited: false,
  };

  const profile: UserProfile = {
    username: 'bike-seller',
    listings: [listing],
  };

  function setUp(): void {
    TestBed.configureTestingModule({
      imports: [UserProfileComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ username: 'bike-seller' }) } },
        },
      ],
    });
    fixture = TestBed.createComponent(UserProfileComponent);
    httpMock = TestBed.inject(HttpTestingController);
  }

  beforeEach(() => {
    setUp();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('displays the username and the ACTIVE listings for that user', () => {
    fixture.detectChanges();

    httpMock.expectOne((request) => request.url === '/api/users/bike-seller').flush(profile);
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    expect(element.querySelector('h1')?.textContent).toContain('bike-seller');
    expect(element.textContent).toContain('Bike');
  });

  it('shows an empty message when the user has no active listings', () => {
    fixture.detectChanges();

    httpMock
      .expectOne((request) => request.url === '/api/users/bike-seller')
      .flush({ username: 'bike-seller', listings: [] });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.empty')).toBeTruthy();
  });

  it('shows a not-found message when the username does not exist', () => {
    fixture.detectChanges();

    httpMock
      .expectOne((request) => request.url === '/api/users/bike-seller')
      .flush({ message: 'No user found with username bike-seller' }, { status: 404, statusText: 'Not Found' });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.not-found')?.textContent).toContain('not found');
  });
});
