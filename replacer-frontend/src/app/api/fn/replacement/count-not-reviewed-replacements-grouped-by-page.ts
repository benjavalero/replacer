/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';

import { PageCount } from '../../models/page-count';

export interface CountNotReviewedReplacementsGroupedByPage$Params {
}

export function countNotReviewedReplacementsGroupedByPage(http: HttpClient, rootUrl: string, params?: CountNotReviewedReplacementsGroupedByPage$Params, context?: HttpContext): Observable<StrictHttpResponse<Array<PageCount>>> {
  const rb = new RequestBuilder(rootUrl, countNotReviewedReplacementsGroupedByPage.PATH, 'get');
  if (params) {
  }

  return http.request(
    rb.build({ responseType: 'json', accept: 'application/json', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return r as StrictHttpResponse<Array<PageCount>>;
    })
  );
}

countNotReviewedReplacementsGroupedByPage.PATH = '/api/replacement/page/count';
