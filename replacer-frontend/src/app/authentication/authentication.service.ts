import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { User } from '../user/user.model';
import { UserService } from '../user/user.service';
import { AuthenticateRequest, AuthenticateResponse, RequestToken } from './authentication.model';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private readonly requestTokenKey = 'requestToken';
  private readonly redirectPathKey = 'redirectPath';
  private readonly baseUrl = `${environment.apiUrl}/authentication`;

  constructor(private httpClient: HttpClient, private userService: UserService) {}

  getAuthenticationUrl$(): Observable<string> {
    return this.getRequestToken$().pipe(
      map((token: RequestToken) => {
        // We keep the request token for further use on verification
        localStorage.setItem(this.requestTokenKey, JSON.stringify(token));

        return token.authorizationUrl;
      })
    );
  }

  private getRequestToken$(): Observable<RequestToken> {
    return this.httpClient.get<RequestToken>(`${this.baseUrl}/request-token`);
  }

  loginUser$(oauthVerifier: string): Observable<User> {
    return this.authenticate$(oauthVerifier).pipe(
      map((response: AuthenticateResponse) => {
        // Remove request token as it is no longer needed
        localStorage.removeItem(this.requestTokenKey);

        // Save user and access token to further use in Wikipedia requests
        const wikipediaUser: User = {
          name: response.name,
          hasRights: response.hasRights,
          admin: response.admin,
          accessToken: {
            token: response.token,
            tokenSecret: response.tokenSecret
          }
        };
        this.userService.setUser(wikipediaUser);
        return wikipediaUser;
      })
    );
  }

  private authenticate$(oauthVerifier: string): Observable<AuthenticateResponse> {
    const requestToken: RequestToken = JSON.parse(localStorage.getItem(this.requestTokenKey));
    const body: AuthenticateRequest = {
      requestToken: requestToken.token,
      requestTokenSecret: requestToken.tokenSecret,
      oauthVerifier: oauthVerifier
    };
    return this.httpClient.post<AuthenticateResponse>(`${this.baseUrl}/authenticate`, body);
  }

  get redirectPath(): string {
    const path = localStorage.getItem(this.redirectPathKey);
    localStorage.removeItem(this.redirectPathKey);
    return path;
  }

  set redirectPath(path: string) {
    localStorage.setItem(this.redirectPathKey, decodeURIComponent(path));
  }
}
