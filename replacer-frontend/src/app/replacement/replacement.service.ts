import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { ReplacementCount } from './replacement-count.model';

@Injectable({
  providedIn: 'root'
})
export class ReplacementService {

  constructor(private httpClient: HttpClient) { }

  findReplacementCounts(): Observable<ReplacementCount[]> {
    return this.httpClient.get<ReplacementCount[]>(`${environment.apiUrl}/article/count/misspellings`);
  }

}
