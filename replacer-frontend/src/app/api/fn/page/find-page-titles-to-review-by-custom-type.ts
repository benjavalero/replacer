/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';

import { PageTitle } from '../../models/page-title';

export interface FindPageTitlesToReviewByCustomType$Params {

/**
 * Replacement kind code
 */
  kind?: number;

/**
 * Replacement subtype
 */
  subtype?: string;

/**
 * If the custom replacement is case-sensitive
 */
  cs?: boolean;

/**
 * Custom replacement suggestion
 */
  suggestion?: string;
}

export function findPageTitlesToReviewByCustomType(http: HttpClient, rootUrl: string, params?: FindPageTitlesToReviewByCustomType$Params, context?: HttpContext): Observable<StrictHttpResponse<Array<PageTitle>>> {
  const rb = new RequestBuilder(rootUrl, findPageTitlesToReviewByCustomType.PATH, 'get');
  if (params) {
    rb.query('kind', params.kind, {});
    rb.query('subtype', params.subtype, {});
    rb.query('cs', params.cs, {});
    rb.query('suggestion', params.suggestion, {});
  }

  return http.request(
    rb.build({ responseType: 'json', accept: 'application/json', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return r as StrictHttpResponse<Array<PageTitle>>;
    })
  );
}

findPageTitlesToReviewByCustomType.PATH = '/api/page/custom';
