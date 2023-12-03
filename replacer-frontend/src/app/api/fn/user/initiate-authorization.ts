/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';

import { RequestToken } from '../../models/request-token';

export interface InitiateAuthorization$Params {
}

export function initiateAuthorization(http: HttpClient, rootUrl: string, params?: InitiateAuthorization$Params, context?: HttpContext): Observable<StrictHttpResponse<RequestToken>> {
  const rb = new RequestBuilder(rootUrl, initiateAuthorization.PATH, 'get');
  if (params) {
  }

  return http.request(
    rb.build({ responseType: 'json', accept: 'application/json', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return r as StrictHttpResponse<RequestToken>;
    })
  );
}

initiateAuthorization.PATH = '/api/user/initiate-authorization';
