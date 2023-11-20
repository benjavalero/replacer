/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';

import { ReplacementType } from '../models/replacement-type';
import { validateCustomReplacement } from '../fn/replacement-type/validate-custom-replacement';
import { ValidateCustomReplacement$Params } from '../fn/replacement-type/validate-custom-replacement';

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
  validateCustomReplacement$Response(params: ValidateCustomReplacement$Params, context?: HttpContext): Observable<StrictHttpResponse<ReplacementType>> {
    return validateCustomReplacement(this.http, this.rootUrl, params, context);
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
  validateCustomReplacement(params: ValidateCustomReplacement$Params, context?: HttpContext): Observable<ReplacementType> {
    return this.validateCustomReplacement$Response(params, context).pipe(
      map((r: StrictHttpResponse<ReplacementType>): ReplacementType => r.body)
    );
  }

}
