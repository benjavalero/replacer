/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';

import { DumpStatus } from '../../models/dump-status';

export interface GetDumpStatus$Params {
}

export function getDumpStatus(http: HttpClient, rootUrl: string, params?: GetDumpStatus$Params, context?: HttpContext): Observable<StrictHttpResponse<DumpStatus>> {
  const rb = new RequestBuilder(rootUrl, getDumpStatus.PATH, 'get');
  if (params) {
  }

  return http.request(
    rb.build({ responseType: 'json', accept: 'application/json', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return r as StrictHttpResponse<DumpStatus>;
    })
  );
}

getDumpStatus.PATH = '/api/dump';
