/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';

import { ReviewerCount } from '../../models/reviewer-count';

export interface CountReviewedReplacementsGroupedByReviewer$Params {
}

export function countReviewedReplacementsGroupedByReviewer(http: HttpClient, rootUrl: string, params?: CountReviewedReplacementsGroupedByReviewer$Params, context?: HttpContext): Observable<StrictHttpResponse<Array<ReviewerCount>>> {
  const rb = new RequestBuilder(rootUrl, countReviewedReplacementsGroupedByReviewer.PATH, 'get');
  if (params) {
  }

  return http.request(
    rb.build({ responseType: 'json', accept: 'application/json', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return r as StrictHttpResponse<Array<ReviewerCount>>;
    })
  );
}

countReviewedReplacementsGroupedByReviewer.PATH = '/api/replacement/user/count';
