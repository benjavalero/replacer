import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { InitiateAuthenticationResponse } from '../../api/models/initiate-authentication-response';
import { RequestToken } from '../../api/models/request-token';
import { VerifyAuthenticationRequest } from '../../api/models/verify-authentication-request';
import { VerifyAuthenticationResponse } from '../../api/models/verify-authentication-response';
import { AuthenticationService } from '../../api/services/authentication.service';
import { User } from '../user/user.model';
import { UserService } from '../user/user.service';

@Injectable()
export class LoginService {
  private readonly requestTokenKey = 'requestToken';
  private readonly redirectPathKey = 'redirectPath';

  constructor(private authenticationService: AuthenticationService, private userService: UserService) {}

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
    return this.authenticationService.initiateAuthentication();
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
    return this.authenticationService.verifyAuthentication({
      body: {
        oauthVerifier,
        requestToken
      } as VerifyAuthenticationRequest
    });
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
