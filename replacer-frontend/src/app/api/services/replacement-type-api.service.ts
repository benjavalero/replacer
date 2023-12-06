/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';

import { findReplacementType } from '../fn/replacement-type/find-replacement-type';
import { FindReplacementType$Params } from '../fn/replacement-type/find-replacement-type';
import { ReplacementType } from '../models/replacement-type';

@Injectable({ providedIn: 'root' })
export class ReplacementTypeApiService extends BaseService {
  constructor(config: ApiConfiguration, http: HttpClient) {
    super(config, http);
  }

  /** Path part for operation `findReplacementType()` */
  static readonly FindReplacementTypePath = '/api/type';

  /**
   * Find a known standard type matching with the given replacement and case-sensitive option.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `findReplacementType()` instead.
   *
   * This method doesn't expect any request body.
   */
  findReplacementType$Response(params: FindReplacementType$Params, context?: HttpContext): Observable<StrictHttpResponse<ReplacementType>> {
    return findReplacementType(this.http, this.rootUrl, params, context);
  }

  /**
   * Find a known standard type matching with the given replacement and case-sensitive option.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `findReplacementType$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  findReplacementType(params: FindReplacementType$Params, context?: HttpContext): Observable<ReplacementType> {
    return this.findReplacementType$Response(params, context).pipe(
      map((r: StrictHttpResponse<ReplacementType>): ReplacementType => r.body)
    );
  }

}
