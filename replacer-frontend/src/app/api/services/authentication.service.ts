/* tslint:disable */
/* eslint-disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpContext } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { RequestBuilder } from '../request-builder';
import { Observable } from 'rxjs';
import { map, filter } from 'rxjs/operators';

import { InitiateAuthenticationResponse } from '../models/initiate-authentication-response';
import { VerifyAuthenticationRequest } from '../models/verify-authentication-request';
import { VerifyAuthenticationResponse } from '../models/verify-authentication-response';

@Injectable({
  providedIn: 'root',
})
export class AuthenticationService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Path part for operation verifyAuthentication
   */
  static readonly VerifyAuthenticationPath = '/api/authentication/verify';

  /**
   * Verify the authorization process.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `verifyAuthentication()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  verifyAuthentication$Response(params: {
    context?: HttpContext
    body: VerifyAuthenticationRequest
  }
): Observable<StrictHttpResponse<VerifyAuthenticationResponse>> {

    const rb = new RequestBuilder(this.rootUrl, AuthenticationService.VerifyAuthenticationPath, 'post');
    if (params) {
      rb.body(params.body, 'application/json');
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<VerifyAuthenticationResponse>;
      })
    );
  }

  /**
   * Verify the authorization process.
   *
   *
   *
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `verifyAuthentication$Response()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  verifyAuthentication(params: {
    context?: HttpContext
    body: VerifyAuthenticationRequest
  }
): Observable<VerifyAuthenticationResponse> {

    return this.verifyAuthentication$Response(params).pipe(
      map((r: StrictHttpResponse<VerifyAuthenticationResponse>) => r.body as VerifyAuthenticationResponse)
    );
  }

  /**
   * Path part for operation initiateAuthentication
   */
  static readonly InitiateAuthenticationPath = '/api/authentication/initiate';

  /**
   * Initiate an authorization process.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `initiateAuthentication()` instead.
   *
   * This method doesn't expect any request body.
   */
  initiateAuthentication$Response(params?: {
    context?: HttpContext
  }
): Observable<StrictHttpResponse<InitiateAuthenticationResponse>> {

    const rb = new RequestBuilder(this.rootUrl, AuthenticationService.InitiateAuthenticationPath, 'get');
    if (params) {
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<InitiateAuthenticationResponse>;
      })
    );
  }

  /**
   * Initiate an authorization process.
   *
   *
   *
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `initiateAuthentication$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  initiateAuthentication(params?: {
    context?: HttpContext
  }
): Observable<InitiateAuthenticationResponse> {

    return this.initiateAuthentication$Response(params).pipe(
      map((r: StrictHttpResponse<InitiateAuthenticationResponse>) => r.body as InitiateAuthenticationResponse)
    );
  }

}
