import { Injectable, Output, EventEmitter } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { RequestToken } from './request-token.model';
import { AccessToken } from './access-token.model';
import { WikipediaUser, Language } from './wikipedia-user.model';
import { AlertService } from '../alert/alert.service';
import { VerificationToken } from './verification-token.model';

@Injectable({
  providedIn: 'root',
})
export class AuthenticationService {
  @Output() userEvent: EventEmitter<WikipediaUser> = new EventEmitter();

  baseUrl = `${environment.apiUrl}/authentication`;

  constructor(
    private httpClient: HttpClient,
    private alertService: AlertService
  ) {}

  isAuthenticated(): boolean {
    return this.accessToken !== null;
  }

  getRequestToken(): Observable<RequestToken> {
    return this.httpClient.get<RequestToken>(`${this.baseUrl}/request-token`);
  }

  getAccessToken(verificationToken: string): Observable<AccessToken> {
    const verificationTokenDto = new VerificationToken();
    verificationTokenDto.requestToken = this.requestToken;
    verificationTokenDto.token = verificationToken;

    const params = new HttpParams()
      .append('requestToken', this.requestToken.token)
      .append('requestTokenSecret', this.requestToken.tokenSecret)
      .append('oauthVerifier', verificationToken);

    return this.httpClient.get<AccessToken>(`${this.baseUrl}/access-token`, {
      params,
    });
  }

  clearSession(): void {
    this.accessToken = null;
    this.user = null;
  }

  get redirectPath(): string {
    return localStorage.getItem('redirectPath');
  }

  set redirectPath(path: string) {
    if (path) {
      localStorage.setItem('redirectPath', path);
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

  get accessToken(): AccessToken {
    return JSON.parse(localStorage.getItem('accessToken'));
  }

  set accessToken(token: AccessToken) {
    if (token) {
      localStorage.setItem('accessToken', JSON.stringify(token));
      this.findUserName();
    } else {
      localStorage.removeItem('accessToken');
    }
  }

  private findUserName(): void {
    const params = new HttpParams()
      .append('accessToken', this.accessToken.token)
      .append('accessTokenSecret', this.accessToken.tokenSecret);

    this.httpClient
      .get<WikipediaUser>(`${this.baseUrl}/user`, { params })
      .subscribe(
        (user: WikipediaUser) => {
          this.user = user;
        },
        (err) => {
          this.alertService.addErrorMessage(
            'Error al buscar el nombre del usuario en sesión'
          );
        }
      );
  }

  get user(): WikipediaUser {
    return JSON.parse(localStorage.getItem('user'));
  }

  set user(user: WikipediaUser) {
    if (user) {
      localStorage.setItem('user', JSON.stringify(user));
    } else {
      localStorage.removeItem('user');
    }
    this.userEvent.emit(user);
  }

  get lang(): Language {
    return this.user ? this.user.lang || Language.es : Language.es;
  }

  set lang(lang: Language) {
    if (this.user) {
      const userAux: WikipediaUser = this.user;
      userAux.lang = lang;
      this.user = userAux;
    }
  }
}
