/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';


export interface FindPageTitlesNotReviewedByType$Params {
  lang: string;

/**
 * Replacement kind code
 */
  kind: number;

/**
 * Replacement subtype
 */
  subtype: string;
}

export function findPageTitlesNotReviewedByType(http: HttpClient, rootUrl: string, params: FindPageTitlesNotReviewedByType$Params, context?: HttpContext): Observable<StrictHttpResponse<string>> {
  const rb = new RequestBuilder(rootUrl, findPageTitlesNotReviewedByType.PATH, 'get');
  if (params) {
    rb.query('lang', params.lang, {});
    rb.query('kind', params.kind, {});
    rb.query('subtype', params.subtype, {});
  }

  return http.request(
    rb.build({ responseType: 'text', accept: 'text/plain', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return r as StrictHttpResponse<string>;
    })
  );
}

findPageTitlesNotReviewedByType.PATH = '/api/page/type';
