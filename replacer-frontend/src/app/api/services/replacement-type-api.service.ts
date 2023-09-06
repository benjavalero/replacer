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

import { ReplacementType } from '../models/replacement-type';

@Injectable({ providedIn: 'root' })
export class ReplacementTypeApiService extends BaseService {
  constructor(config: ApiConfiguration, http: HttpClient) {
    super(config, http);
  }

  /** Path part for operation `validateCustomReplacement()` */
  static readonly ValidateCustomReplacementPath = '/api/type/validate';

  /**
   * Validate if the custom replacement matches with a known replacement type.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `validateCustomReplacement()` instead.
   *
   * This method doesn't expect any request body.
   */
  validateCustomReplacement$Response(
    params: {

    /**
     * Replacement to validate
     */
      replacement: string;

    /**
     * If the custom replacement is case-sensitive
     */
      cs: boolean;
    },
    context?: HttpContext
  ): Observable<StrictHttpResponse<ReplacementType>> {
    const rb = new RequestBuilder(this.rootUrl, ReplacementTypeApiService.ValidateCustomReplacementPath, 'get');
    if (params) {
      rb.query('replacement', params.replacement, {});
      rb.query('cs', params.cs, {});
    }

    return this.http.request(
      rb.build({ responseType: 'json', accept: 'application/json', context })
    ).pipe(
      filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<ReplacementType>;
      })
    );
  }

  /**
   * Validate if the custom replacement matches with a known replacement type.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `validateCustomReplacement$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  validateCustomReplacement(
    params: {

    /**
     * Replacement to validate
     */
      replacement: string;

    /**
     * If the custom replacement is case-sensitive
     */
      cs: boolean;
    },
    context?: HttpContext
  ): Observable<ReplacementType> {
    return this.validateCustomReplacement$Response(params, context).pipe(
      map((r: StrictHttpResponse<ReplacementType>): ReplacementType => r.body)
    );
  }

}
