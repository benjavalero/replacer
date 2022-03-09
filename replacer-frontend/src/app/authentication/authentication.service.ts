import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { User } from '../user/user.model';
import { UserService } from '../user/user.service';
import {
  VerifyAuthenticationRequest,
  VerifyAuthenticationResponse,
  InitiateAuthenticationResponse,
  RequestToken
} from './authentication.model';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private readonly requestTokenKey = 'requestToken';
  private readonly redirectPathKey = 'redirectPath';
  private readonly baseUrl = `${environment.apiUrl}/authentication`;

  constructor(private httpClient: HttpClient, private userService: UserService) {}

  getAuthenticationUrl$(): Observable<string> {
    return this.initiateAuthentication$().pipe(
      map((token: InitiateAuthenticationResponse) => {
        // We keep the request token for further use on verification
        localStorage.setItem(this.requestTokenKey, JSON.stringify(token.requestToken));

        return token.authorizationUrl;
      })
    );
  }

  private initiateAuthentication$(): Observable<InitiateAuthenticationResponse> {
    return this.httpClient.get<InitiateAuthenticationResponse>(`${this.baseUrl}/initiate`);
  }

  loginUser$(oauthVerifier: string): Observable<User> {
    return this.authenticate$(oauthVerifier).pipe(
      map((response: VerifyAuthenticationResponse) => {
        // Remove request token as it is no longer needed
        localStorage.removeItem(this.requestTokenKey);

        // Save user and access token to further use in Wikipedia requests
        const wikipediaUser = new User(response);
        this.userService.setUser(wikipediaUser);
        return wikipediaUser;
      })
    );
  }

  private authenticate$(oauthVerifier: string): Observable<VerifyAuthenticationResponse> {
    // At this point we can assert that the request token exists
    const requestToken: RequestToken = JSON.parse(localStorage.getItem(this.requestTokenKey)!);
    const body = new VerifyAuthenticationRequest(requestToken, oauthVerifier);
    return this.httpClient.post<VerifyAuthenticationResponse>(`${this.baseUrl}/verify`, body);
  }

  get redirectPath(): string {
    const path = localStorage.getItem(this.redirectPathKey);
    localStorage.removeItem(this.redirectPathKey);
    return path || '';
  }

  set redirectPath(path: string) {
    localStorage.setItem(this.redirectPathKey, decodeURIComponent(path));
  }
}
