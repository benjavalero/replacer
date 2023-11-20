/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';

import { ReplacementCount } from '../../models/replacement-count';

export interface CountReplacements$Params {

/**
 * Filter by reviewed/unreviewed replacements
 */
  reviewed: boolean;
}

export function countReplacements(http: HttpClient, rootUrl: string, params: CountReplacements$Params, context?: HttpContext): Observable<StrictHttpResponse<ReplacementCount>> {
  const rb = new RequestBuilder(rootUrl, countReplacements.PATH, 'get');
  if (params) {
    rb.query('reviewed', params.reviewed, {});
  }

  return http.request(
    rb.build({ responseType: 'json', accept: 'application/json', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return r as StrictHttpResponse<ReplacementCount>;
    })
  );
}

countReplacements.PATH = '/api/replacement/count';
