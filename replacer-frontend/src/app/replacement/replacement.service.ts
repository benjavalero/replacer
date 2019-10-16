import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { ReplacementCountList } from './replacement-count-list.model';

@Injectable({
  providedIn: 'root'
})
export class ReplacementService {

  constructor(private httpClient: HttpClient) { }

  findReplacementCounts(): Observable<ReplacementCountList[]> {
    return this.httpClient.get<ReplacementCountList[]>(`${environment.apiUrl}/replacement/count/grouped`);
  }

}
