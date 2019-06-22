import { Injectable, Output, EventEmitter } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { OauthToken } from './oauth-token.model';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  @Output() usernameEvent: EventEmitter<string> = new EventEmitter();

  constructor(private httpClient: HttpClient) { }

  isAuthenticated(): boolean {
    return this.accessToken !== null;
  }

  generateRequestToken(): Observable<OauthToken> {
    return this.httpClient.get<OauthToken>(`${environment.apiUrl}/authentication/requestToken`);
  }

  generateAuthorizationUrl(): Observable<string> {
    let params = new HttpParams();
    params = params.append('token', this.requestToken.token);

    return this.httpClient.get<string>(`${environment.apiUrl}/authentication/authorizationUrl`, { params });
  }

  generateAccessToken(verificationToken: string): Observable<OauthToken> {
    let params = new HttpParams();
    params = params.append('token', this.requestToken.token);
    params = params.append('tokenSecret', this.requestToken.tokenSecret);
    params = params.append('verificationToken', verificationToken);

    return this.httpClient.get<OauthToken>(`${environment.apiUrl}/authentication/accessToken`, { params });
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

  get requestToken(): OauthToken {
    return JSON.parse(localStorage.getItem('requestToken'));
  }

  set requestToken(token: OauthToken) {
    if (token) {
      localStorage.setItem('requestToken', JSON.stringify(token));
    } else {
      localStorage.removeItem('requestToken');
    }
  }

  get accessToken(): OauthToken {
    return JSON.parse(localStorage.getItem('accessToken'));
  }

  set accessToken(token: OauthToken) {
    localStorage.setItem('accessToken', JSON.stringify(token));
    this.findUserName();
  }

  private findUserName(): void {
    let params = new HttpParams();
    params = params.append('token', this.accessToken.token);
    params = params.append('tokenSecret', this.accessToken.tokenSecret);

    this.httpClient.get<string>(`${environment.apiUrl}/authentication/username`, { params })
      .subscribe((username: string) => {
        this.username = username;
      });
  }

  get username(): string {
    return localStorage.getItem('username');
  }

  set username(username: string) {
    if (username) {
      localStorage.setItem('username', username);
    } else {
      localStorage.removeItem('username');
    }
    this.usernameEvent.emit(username);
  }

}
