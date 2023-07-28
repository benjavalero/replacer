/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';

import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { RequestBuilder } from '../request-builder';

import { InitiateAuthorizationResponse } from '../models/initiate-authorization-response';
import { User } from '../models/user';
import { VerifyAuthorizationRequest } from '../models/verify-authorization-request';

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
  verifyAuthorization$Response(
    params: {
      body: VerifyAuthorizationRequest
    },
    context?: HttpContext
  ): Observable<StrictHttpResponse<User>> {
    const rb = new RequestBuilder(this.rootUrl, UserApiService.VerifyAuthorizationPath, 'post');
    if (params) {
      rb.body(params.body, 'application/json');
    }

    return this.http.request(
      rb.build({ responseType: 'json', accept: 'application/json', context })
    ).pipe(
      filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<User>;
      })
    );
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
  verifyAuthorization(
    params: {
      body: VerifyAuthorizationRequest
    },
    context?: HttpContext
  ): Observable<User> {
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
  initiateAuthorization$Response(
    params?: {
    },
    context?: HttpContext
  ): Observable<StrictHttpResponse<InitiateAuthorizationResponse>> {
    const rb = new RequestBuilder(this.rootUrl, UserApiService.InitiateAuthorizationPath, 'get');
    if (params) {
    }

    return this.http.request(
      rb.build({ responseType: 'json', accept: 'application/json', context })
    ).pipe(
      filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<InitiateAuthorizationResponse>;
      })
    );
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
  initiateAuthorization(
    params?: {
    },
    context?: HttpContext
  ): Observable<InitiateAuthorizationResponse> {
    return this.initiateAuthorization$Response(params, context).pipe(
      map((r: StrictHttpResponse<InitiateAuthorizationResponse>): InitiateAuthorizationResponse => r.body)
    );
  }

}
