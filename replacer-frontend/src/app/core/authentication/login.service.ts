import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { RequestToken } from '../../api/models/request-token';
import { User } from '../../api/models/user';
import { UserService } from '../user/user.service';
import { UserApiService } from '../../api/services/user-api.service';
import { InitiateAuthorizationResponse } from '../../api/models/initiate-authorization-response';
import { VerifyAuthorizationRequest } from '../../api/models/verify-authorization-request';

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  private readonly requestTokenKey = 'requestToken';
  private readonly redirectPathKey = 'redirectPath';

  constructor(
    private userApiService: UserApiService,
    private userService: UserService
  ) {}

  getAuthorizationUrl$(): Observable<string> {
    return this.userApiService.initiateAuthorization().pipe(
      map((token: InitiateAuthorizationResponse) => {
        // We keep the request token for further use on verification
        localStorage.setItem(this.requestTokenKey, JSON.stringify(token.requestToken));

        return token.authorizationUrl;
      })
    );
  }

  loginUser$(oauthVerifier: string): Observable<User> {
    return this.authorize$(oauthVerifier).pipe(
      map((wikipediaUser: User) => {
        // Remove request token as it is no longer needed
        localStorage.removeItem(this.requestTokenKey);

        // Save user and access token to further use in Wikipedia requests
        this.userService.setUser(wikipediaUser);
        return wikipediaUser;
      })
    );
  }

  private authorize$(oauthVerifier: string): Observable<User> {
    // At this point we can assert that the request token exists
    const requestToken: RequestToken = JSON.parse(localStorage.getItem(this.requestTokenKey)!);
    return this.userApiService.verifyAuthorization({
      body: {
        oauthVerifier,
        requestToken
      } as VerifyAuthorizationRequest
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
