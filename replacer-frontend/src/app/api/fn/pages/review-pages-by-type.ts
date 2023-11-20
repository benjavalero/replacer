/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';


export interface ReviewPagesByType$Params {

/**
 * Replacement kind code
 */
  kind: number;

/**
 * Replacement subtype
 */
  subtype: string;
}

export function reviewPagesByType(http: HttpClient, rootUrl: string, params: ReviewPagesByType$Params, context?: HttpContext): Observable<StrictHttpResponse<void>> {
  const rb = new RequestBuilder(rootUrl, reviewPagesByType.PATH, 'post');
  if (params) {
    rb.query('kind', params.kind, {});
    rb.query('subtype', params.subtype, {});
  }

  return http.request(
    rb.build({ responseType: 'text', accept: '*/*', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return (r as HttpResponse<any>).clone({ body: undefined }) as StrictHttpResponse<void>;
    })
  );
}

reviewPagesByType.PATH = '/api/page/type';
