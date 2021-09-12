import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { AccessToken, User } from './user.model';

export const USER_PARAM = 'user';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly wikipediaUserKey = 'wikipediaUser';
  private readonly _user = new BehaviorSubject<User>(this.emptyUser());

  constructor() {
    this.loadUser();
  }

  get user$(): Observable<User> {
    return this._user.asObservable();
  }

  get accessToken(): AccessToken {
    return this._user.getValue().accessToken;
  }

  get userName(): string {
    return this._user.getValue().name;
  }

  private loadUser(): void {
    // TODO: We clean the old user key. This line must be removed in the future.
    localStorage.removeItem('user');

    let user = this.emptyUser();
    const localWikipediaUser = localStorage.getItem(this.wikipediaUserKey);
    if (localWikipediaUser) {
      const localUser: User = JSON.parse(localWikipediaUser);
      if (this.isValid(localUser)) {
        user = localUser;
      }
    }

    this._user.next(user);
  }

  private emptyUser(): User {
    return {} as User;
  }

  isValidUser(): boolean {
    return this.isValid(this._user.getValue());
  }

  private isValid(user: User): boolean {
    return (
      user != null && user.name != null && user.hasRights != null && user.admin != null && user.accessToken != null
    );
  }

  hasRightsUser(): boolean {
    return this._user.getValue().hasRights;
  }

  isBotUser(): boolean {
    return this._user.getValue().bot;
  }

  clearSession(): void {
    this.setUser(this.emptyUser());
  }

  setUser(user: User): void {
    localStorage.setItem(this.wikipediaUserKey, JSON.stringify(user));
    this._user.next(user);
  }
}
