import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface RegisteredUser {
  id: number;
  username: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private readonly http: HttpClient) {}

  register(username: string, password: string): Observable<RegisteredUser> {
    return this.http.post<RegisteredUser>('/api/auth/register', { username, password });
  }
}
