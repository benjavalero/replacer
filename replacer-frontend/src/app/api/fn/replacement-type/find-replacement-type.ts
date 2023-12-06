/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';

import { ReplacementType } from '../../models/replacement-type';

export interface FindReplacementType$Params {

/**
 * Text to replace
 */
  replacement: string;

/**
 * If the replacement is case-sensitive
 */
  cs: boolean;
}

export function findReplacementType(http: HttpClient, rootUrl: string, params: FindReplacementType$Params, context?: HttpContext): Observable<StrictHttpResponse<ReplacementType>> {
  const rb = new RequestBuilder(rootUrl, findReplacementType.PATH, 'get');
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

findReplacementType.PATH = '/api/type';
