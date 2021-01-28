import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { RequestToken } from './request-token.model';
import { Language, LANG_DEFAULT, WikipediaUser } from './wikipedia-user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  readonly baseUrl = `${environment.apiUrl}/authentication`;
  readonly wikipediaUserKey = 'wikipediaUser';

  private readonly _user = new BehaviorSubject<WikipediaUser>(this.getLocalUser());
  readonly user$ = this._user.asObservable();

  private readonly _lang = new BehaviorSubject<Language>(this.getLocalLang());
  readonly lang$ = this._lang.asObservable();

  constructor(private httpClient: HttpClient) {}

  isAuthenticated(): boolean {
    return this.user !== null;
  }

  getAuthenticationUrl$(): Observable<string> {
    return this.getRequestToken$().pipe(
      map((token: RequestToken) => {
        // We keep the request token for further use on verification
        this.requestToken = token;

        return token.authorizationUrl;
      })
    );
  }

  private getRequestToken$(): Observable<RequestToken> {
    return this.httpClient.get<RequestToken>(`${this.baseUrl}/request-token`);
  }

  loginUser$(oauthVerifier: string): Observable<WikipediaUser> {
    return this.getAccessToken$(oauthVerifier).pipe(
      map((wikipediaUser: WikipediaUser) => {
        // Remove request token as it is no longer needed
        this.requestToken = null;

        // Save user and access token to further use in Wikipedia requests
        this.user = wikipediaUser;

        return wikipediaUser;
      })
    );
  }

  private getAccessToken$(verificationToken: string): Observable<WikipediaUser> {
    const params = new HttpParams()
      .append('requestToken', this.requestToken.token)
      .append('requestTokenSecret', this.requestToken.tokenSecret)
      .append('oauthVerifier', verificationToken);

    return this.httpClient.get<WikipediaUser>(`${this.baseUrl}/access-token`, {
      params
    });
  }

  clearSession(): void {
    this.user = null;
  }

  get redirectPath(): string {
    return localStorage.getItem('redirectPath');
  }

  set redirectPath(path: string) {
    if (path) {
      localStorage.setItem('redirectPath', decodeURIComponent(path));
    } else {
      localStorage.removeItem('redirectPath');
    }
  }

  get requestToken(): RequestToken {
    return JSON.parse(localStorage.getItem('requestToken'));
  }

  set requestToken(token: RequestToken) {
    if (token) {
      localStorage.setItem('requestToken', JSON.stringify(token));
    } else {
      localStorage.removeItem('requestToken');
    }
  }

  private getLocalUser(): WikipediaUser {
    return JSON.parse(localStorage.getItem(this.wikipediaUserKey));
  }

  get user(): WikipediaUser {
    return this._user.getValue();
  }

  set user(user: WikipediaUser) {
    if (user) {
      localStorage.setItem(this.wikipediaUserKey, JSON.stringify(user));
    } else {
      localStorage.removeItem(this.wikipediaUserKey);
    }
    this._user.next(user);
  }

  private getLocalLang(): Language {
    return JSON.parse(localStorage.getItem('lang')) || LANG_DEFAULT;
  }

  get lang(): Language {
    return this._lang.getValue();
  }

  set lang(lang: Language) {
    if (lang) {
      localStorage.setItem('lang', JSON.stringify(lang));
    } else {
      localStorage.removeItem('lang');
    }
    this._lang.next(lang);
  }
}
