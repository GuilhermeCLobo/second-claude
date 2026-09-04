import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Category } from './category';
import { Listing } from './listing.model';

export interface CreateListingRequest {
  title: string;
  description: string;
  price: number;
  category: Category;
}

@Injectable({ providedIn: 'root' })
export class ListingsService {
  constructor(private readonly http: HttpClient) {}

  browse(category?: Category): Observable<Listing[]> {
    let params = new HttpParams();
    if (category) {
      params = params.set('category', category);
    }
    return this.http.get<Listing[]>('/api/listings', { params });
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

  myPosted(): Observable<Listing[]> {
    return this.http.get<Listing[]>('/api/listings/mine/posted');
  }

  myBought(): Observable<Listing[]> {
    return this.http.get<Listing[]>('/api/listings/mine/bought');
  }
}
