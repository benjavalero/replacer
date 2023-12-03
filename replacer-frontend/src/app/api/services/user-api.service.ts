/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';

import { initiateAuthorization } from '../fn/user/initiate-authorization';
import { InitiateAuthorization$Params } from '../fn/user/initiate-authorization';
import { RequestToken } from '../models/request-token';
import { User } from '../models/user';
import { verifyAuthorization } from '../fn/user/verify-authorization';
import { VerifyAuthorization$Params } from '../fn/user/verify-authorization';

@Injectable({ providedIn: 'root' })
export class UserApiService extends BaseService {
  constructor(config: ApiConfiguration, http: HttpClient) {
    super(config, http);
  }

  /** Path part for operation `verifyAuthorization()` */
  static readonly VerifyAuthorizationPath = '/api/user/verify-authorization';

  /**
   * Verify the authorization process.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `verifyAuthorization()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  verifyAuthorization$Response(params: VerifyAuthorization$Params, context?: HttpContext): Observable<StrictHttpResponse<User>> {
    return verifyAuthorization(this.http, this.rootUrl, params, context);
  }

  /**
   * Verify the authorization process.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `verifyAuthorization$Response()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  verifyAuthorization(params: VerifyAuthorization$Params, context?: HttpContext): Observable<User> {
    return this.verifyAuthorization$Response(params, context).pipe(
      map((r: StrictHttpResponse<User>): User => r.body)
    );
  }

  /** Path part for operation `initiateAuthorization()` */
  static readonly InitiateAuthorizationPath = '/api/user/initiate-authorization';

  /**
   * Initiate an authorization process.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `initiateAuthorization()` instead.
   *
   * This method doesn't expect any request body.
   */
  initiateAuthorization$Response(params?: InitiateAuthorization$Params, context?: HttpContext): Observable<StrictHttpResponse<RequestToken>> {
    return initiateAuthorization(this.http, this.rootUrl, params, context);
  }

  /**
   * Initiate an authorization process.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `initiateAuthorization$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  initiateAuthorization(params?: InitiateAuthorization$Params, context?: HttpContext): Observable<RequestToken> {
    return this.initiateAuthorization$Response(params, context).pipe(
      map((r: StrictHttpResponse<RequestToken>): RequestToken => r.body)
    );
  }

}
