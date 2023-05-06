import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { InitiateAuthenticationResponse } from '../../api/models/initiate-authentication-response';
import { RequestToken } from '../../api/models/request-token';
import { User } from '../../api/models/user';
import { VerifyAuthenticationRequest } from '../../api/models/verify-authentication-request';
import { AuthenticationService } from '../../api/services/authentication.service';
import { UserService } from '../user/user.service';

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  private readonly requestTokenKey = 'requestToken';
  private readonly redirectPathKey = 'redirectPath';

  constructor(private authenticationService: AuthenticationService, private userService: UserService) {}

  getAuthenticationUrl$(): Observable<string> {
    return this.authenticationService.initiateAuthentication().pipe(
      map((token: InitiateAuthenticationResponse) => {
        // We keep the request token for further use on verification
        localStorage.setItem(this.requestTokenKey, JSON.stringify(token.requestToken));

        return token.authorizationUrl;
      })
    );
  }

  loginUser$(oauthVerifier: string): Observable<User> {
    return this.authenticate$(oauthVerifier).pipe(
      map((wikipediaUser: User) => {
        // Remove request token as it is no longer needed
        localStorage.removeItem(this.requestTokenKey);

        // Save user and access token to further use in Wikipedia requests
        this.userService.setUser(wikipediaUser);
        return wikipediaUser;
      })
    );
  }

  private authenticate$(oauthVerifier: string): Observable<User> {
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
