import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { UserProfile } from './user-profile.model';

@Injectable({ providedIn: 'root' })
export class UserProfileService {
  constructor(private readonly http: HttpClient) {}

  getProfile(username: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`/api/users/${username}`);
  }
}
