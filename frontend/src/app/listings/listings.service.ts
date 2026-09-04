import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Category } from './category';
import { BrowseListingsParams, BrowseListingsResult, Listing } from './listing.model';

export interface CreateListingRequest {
  title: string;
  description: string;
  price: number;
  category: Category;
}

@Injectable({ providedIn: 'root' })
export class ListingsService {
  constructor(private readonly http: HttpClient) {}

  browse(params: BrowseListingsParams = {}): Observable<BrowseListingsResult> {
    let httpParams = new HttpParams();
    if (params.category) {
      httpParams = httpParams.set('category', params.category);
    }
    if (params.search) {
      httpParams = httpParams.set('search', params.search);
    }
    if (params.minPrice != null) {
      httpParams = httpParams.set('minPrice', params.minPrice);
    }
    if (params.maxPrice != null) {
      httpParams = httpParams.set('maxPrice', params.maxPrice);
    }
    if (params.sort) {
      httpParams = httpParams.set('sort', params.sort);
    }
    if (params.page != null) {
      httpParams = httpParams.set('page', params.page);
    }
    if (params.size != null) {
      httpParams = httpParams.set('size', params.size);
    }
    return this.http.get<BrowseListingsResult>('/api/listings', { params: httpParams });
  }

  getById(id: number): Observable<Listing> {
    return this.http.get<Listing>(`/api/listings/${id}`);
  }

  create(request: CreateListingRequest, photo: File): Observable<Listing> {
    const formData = new FormData();
    formData.append('listing', new Blob([JSON.stringify(request)], { type: 'application/json' }));
    formData.append('photo', photo);
    return this.http.post<Listing>('/api/listings', formData);
  }

  update(id: number, request: CreateListingRequest): Observable<Listing> {
    return this.http.put<Listing>(`/api/listings/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/listings/${id}`);
  }

  buy(id: number): Observable<Listing> {
    return this.http.post<Listing>(`/api/listings/${id}/buy`, {});
  }

  addPhoto(id: number, photo: File): Observable<Listing> {
    const formData = new FormData();
    formData.append('photo', photo);
    return this.http.post<Listing>(`/api/listings/${id}/photos`, formData);
  }

  removePhoto(id: number, photoId: number): Observable<Listing> {
    return this.http.delete<Listing>(`/api/listings/${id}/photos/${photoId}`);
  }

  reorderPhotos(id: number, photoIds: number[]): Observable<Listing> {
    return this.http.put<Listing>(`/api/listings/${id}/photos/order`, { photoIds });
  }

  myPosted(): Observable<Listing[]> {
    return this.http.get<Listing[]>('/api/listings/mine/posted');
  }

  myBought(): Observable<Listing[]> {
    return this.http.get<Listing[]>('/api/listings/mine/bought');
  }
}
