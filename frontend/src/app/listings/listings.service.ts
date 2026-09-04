import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Category } from './category';
import { Listing } from './listing.model';

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
}
