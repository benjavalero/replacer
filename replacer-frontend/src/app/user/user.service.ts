import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { AccessToken } from '../authentication/access-token.model';
import { WikipediaUser } from '../authentication/wikipedia-user.model';

export const USER_PARAM = 'user';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly wikipediaUserKey = 'wikipediaUser';
  private readonly _user = new BehaviorSubject<WikipediaUser>(this.emptyUser());

  constructor() {
    this.loadUser();
  }

  get user$(): Observable<WikipediaUser> {
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

    let user = JSON.parse(localStorage.getItem(this.wikipediaUserKey));
    if (!this.isValid(user)) {
      user = this.emptyUser();
    }

    this._user.next(user);
  }

  private emptyUser(): WikipediaUser {
    return { name: null, admin: false, accessToken: null };
  }

  isValidUser(): boolean {
    return this.isValid(this._user.getValue());
  }

  private isValid(user: WikipediaUser): boolean {
    return user != null && user.name != null && user.admin != null && user.accessToken != null;
  }

  clearSession(): void {
    this.setUser(this.emptyUser());
  }

  setUser(user: WikipediaUser): void {
    localStorage.setItem(this.wikipediaUserKey, JSON.stringify(user));
    this._user.next(user);
  }
}
