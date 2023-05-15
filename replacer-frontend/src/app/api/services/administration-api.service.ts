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

import { PublicIp } from '../models/public-ip';

@Injectable({
  providedIn: 'root',
})
export class AdministrationApiService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Path part for operation getPublicIp
   */
  static readonly GetPublicIpPath = '/api/admin/public-ip';

  /**
   * Get the public IP of the application used to perform the editions in Wikipedia.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `getPublicIp()` instead.
   *
   * This method doesn't expect any request body.
   */
  getPublicIp$Response(params?: {
    context?: HttpContext
  }
): Observable<StrictHttpResponse<PublicIp>> {

    const rb = new RequestBuilder(this.rootUrl, AdministrationApiService.GetPublicIpPath, 'get');
    if (params) {
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<PublicIp>;
      })
    );
  }

  /**
   * Get the public IP of the application used to perform the editions in Wikipedia.
   *
   *
   *
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `getPublicIp$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  getPublicIp(params?: {
    context?: HttpContext
  }
): Observable<PublicIp> {

    return this.getPublicIp$Response(params).pipe(
      map((r: StrictHttpResponse<PublicIp>) => r.body as PublicIp)
    );
  }

}
