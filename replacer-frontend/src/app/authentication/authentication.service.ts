import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { OauthToken } from './oauth-token.model';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

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
    return sessionStorage.getItem('redirectPath');
  }

  set redirectPath(path: string) {
    if (path) {
      sessionStorage.setItem('redirectPath', path);
    } else {
      sessionStorage.removeItem('redirectPath');
    }
  }

  get requestToken(): OauthToken {
    return JSON.parse(sessionStorage.getItem('requestToken'));
  }

  set requestToken(token: OauthToken) {
    if (token) {
      sessionStorage.setItem('requestToken', JSON.stringify(token));
    } else {
      sessionStorage.removeItem('requestToken');
    }
  }

  get accessToken(): OauthToken {
    return JSON.parse(sessionStorage.getItem('accessToken'));
  }

  set accessToken(token: OauthToken) {
    sessionStorage.setItem('accessToken', JSON.stringify(token));
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
    return sessionStorage.getItem('username');
  }

  set username(username: string) {
    if (username) {
      sessionStorage.setItem('username', username);
    } else {
      sessionStorage.removeItem('username');
    }
  }

}
