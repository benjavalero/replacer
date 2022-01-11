import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { AccessToken, User } from './user.model';

export const USER_PARAM = 'user';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly wikipediaUserKey = 'wikipediaUser';
  readonly user$ = new BehaviorSubject<User>(this.emptyUser());

  constructor() {
    this.loadUser();
  }

  get accessToken(): AccessToken {
    return this.user$.getValue().accessToken;
  }

  get userName(): string {
    return this.user$.getValue().name;
  }

  private loadUser(): void {
    let user = this.emptyUser();
    const localWikipediaUser = localStorage.getItem(this.wikipediaUserKey);
    if (localWikipediaUser) {
      const localUser: User = JSON.parse(localWikipediaUser);
      if (this.isValid(localUser)) {
        user = localUser;
      }
    }

    this.user$.next(user);
  }

  private emptyUser(): User {
    return {} as User;
  }

  isValidUser(): boolean {
    return this.isValid(this.user$.getValue());
  }

  private isValid(user: User): boolean {
    return (
      user != null && user.name != null && user.hasRights != null && user.admin != null && user.accessToken != null
    );
  }

  hasRightsUser(): boolean {
    return this.user$.getValue().hasRights;
  }

  isBotUser(): boolean {
    return this.user$.getValue().bot;
  }

  clearSession(): void {
    this.setUser(this.emptyUser());
  }

  setUser(user: User): void {
    localStorage.setItem(this.wikipediaUserKey, JSON.stringify(user));
    this.user$.next(user);
  }
}
