import { computed, Injectable, signal } from '@angular/core';
import { User } from '../../api/models/user';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly wikipediaUserKey = 'wikipediaUser';
  private readonly user = signal<User>(this.loadCurrentUser());
  readonly isValidUser = computed(() => this.user().name !== '');
  readonly hasRightsUser = computed(() => this.user().hasRights);
  readonly isBotUser = computed(() => this.user().bot);
  readonly isAdminUser = computed(() => this.user().admin);
  readonly userName = computed(() => this.user().name);

  private loadCurrentUser(): User {
    const localWikipediaUser = localStorage.getItem(this.wikipediaUserKey);
    if (localWikipediaUser !== null) {
      return JSON.parse(localWikipediaUser) as User;
    } else {
      return this.buildNotValidUser();
    }
  }

  private buildNotValidUser(): User {
    return {
      name: '',
      hasRights: false,
      bot: false,
      admin: false
    } as User;
  }

  clearSession(): void {
    this.setUser(null);
  }

  setUser(user: User | null): void {
    if (user !== null) {
      localStorage.setItem(this.wikipediaUserKey, JSON.stringify(user));
      this.user.set(user);
    } else {
      localStorage.removeItem(this.wikipediaUserKey);
      this.user.set(this.buildNotValidUser());
    }
  }
}
