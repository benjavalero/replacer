/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';

import { ReplacementType } from '../../models/replacement-type';

export interface ValidateCustomReplacement$Params {

/**
 * Replacement to validate
 */
  replacement: string;

/**
 * If the custom replacement is case-sensitive
 */
  cs: boolean;
}

export function validateCustomReplacement(http: HttpClient, rootUrl: string, params: ValidateCustomReplacement$Params, context?: HttpContext): Observable<StrictHttpResponse<ReplacementType>> {
  const rb = new RequestBuilder(rootUrl, validateCustomReplacement.PATH, 'get');
  if (params) {
    rb.query('replacement', params.replacement, {});
    rb.query('cs', params.cs, {});
  }

  return http.request(
    rb.build({ responseType: 'json', accept: 'application/json', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return r as StrictHttpResponse<ReplacementType>;
    })
  );
}

validateCustomReplacement.PATH = '/api/type/validate';
