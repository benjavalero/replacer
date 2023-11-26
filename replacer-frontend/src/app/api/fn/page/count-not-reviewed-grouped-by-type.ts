/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';

import { KindCount } from '../../models/kind-count';

export interface CountNotReviewedGroupedByType$Params {
}

export function countNotReviewedGroupedByType(http: HttpClient, rootUrl: string, params?: CountNotReviewedGroupedByType$Params, context?: HttpContext): Observable<StrictHttpResponse<Array<KindCount>>> {
  const rb = new RequestBuilder(rootUrl, countNotReviewedGroupedByType.PATH, 'get');
  if (params) {
  }

  return http.request(
    rb.build({ responseType: 'json', accept: 'application/json', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return r as StrictHttpResponse<Array<KindCount>>;
    })
  );
}

countNotReviewedGroupedByType.PATH = '/api/page/type/count';
