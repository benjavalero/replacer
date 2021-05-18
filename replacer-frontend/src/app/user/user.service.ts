import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { User } from './user.model';

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

  get user(): User {
    return this._user.getValue();
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

  private emptyUser(): User {
    return {} as User;
  }

  isValidUser(): boolean {
    return this.isValid(this._user.getValue());
  }

  private isValid(user: User): boolean {
    return (
      user != null && user.name != null && user.hasRights != null && user.admin != null && user.token != null
    );
  }

  hasRightsUser(): boolean {
    return this._user.getValue().hasRights;
  }

  clearSession(): void {
    this.setUser(this.emptyUser());
  }

  setUser(user: User): void {
    localStorage.setItem(this.wikipediaUserKey, JSON.stringify(user));
    this._user.next(user);
  }
}
