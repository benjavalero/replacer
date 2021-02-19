import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { UserService } from '../user/user.service';
import { RequestToken } from './request-token.model';
import { WikipediaUser } from './wikipedia-user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  readonly baseUrl = `${environment.apiUrl}/authentication`;

  constructor(private httpClient: HttpClient, private userService: UserService) {}

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
    return this.getLoggedUser$(oauthVerifier).pipe(
      map((wikipediaUser: WikipediaUser) => {
        // Remove request token as it is no longer needed
        this.requestToken = null;

        // Save user and access token to further use in Wikipedia requests
        this.userService.setUser(wikipediaUser);

        return wikipediaUser;
      })
    );
  }

  private getLoggedUser$(verificationToken: string): Observable<WikipediaUser> {
    const params = new HttpParams()
      .append('requestToken', this.requestToken.token)
      .append('requestTokenSecret', this.requestToken.tokenSecret)
      .append('oauthVerifier', verificationToken);

    return this.httpClient.get<WikipediaUser>(`${this.baseUrl}/logged-user`, {
      params
    });
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
}
