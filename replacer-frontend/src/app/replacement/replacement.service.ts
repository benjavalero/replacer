import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../../environments/environment';
import { ReplacementCount } from './replacement-count.model';

@Injectable({
  providedIn: 'root'
})
export class ReplacementService {

  constructor(private httpClient: HttpClient) { }

  findReplacementCounts(): Observable<ReplacementCount[]> {
    return this.httpClient.get(`${environment.apiUrl}/article/count/replacements/grouped`).pipe(
      map((data: any[]) => data.map((item: any) => {
        const model = new ReplacementCount();
        model.type = item.t;
        model.subtype = item.s;
        model.count = item.c;
        return model;
      }))
    );
  }

}
