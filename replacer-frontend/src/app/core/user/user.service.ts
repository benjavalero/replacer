import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { AccessToken } from '../../api/models/access-token';
import { User } from './user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly wikipediaUserKey = 'wikipediaUser';
  readonly user$ = new BehaviorSubject<User | null>(null);

  constructor() {
    this.loadUser();
  }

  get accessToken(): AccessToken | undefined {
    return this.user$.getValue()?.accessToken;
  }

  get userName(): string | undefined {
    return this.user$.getValue()?.name;
  }

  private loadUser(): void {
    let user: User | null = null;
    const localWikipediaUser = localStorage.getItem(this.wikipediaUserKey);
    if (localWikipediaUser) {
      user = JSON.parse(localWikipediaUser) as User;
    }

    this.user$.next(user);
  }

  isValidUser(): boolean {
    return this.user$.getValue() !== null;
  }

  hasRightsUser(): boolean {
    return this.user$.getValue()?.hasRights || false;
  }

  isBotUser(): boolean {
    return this.user$.getValue()?.bot || false;
  }

  clearSession(): void {
    this.setUser(null);
  }

  setUser(user: User | null): void {
    if (user) {
      localStorage.setItem(this.wikipediaUserKey, JSON.stringify(user));
    } else {
      localStorage.removeItem(this.wikipediaUserKey);
    }
    this.user$.next(user);
  }
}
