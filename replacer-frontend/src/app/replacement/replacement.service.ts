import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { ReplacementCountList } from './replacement-count-list.model';
import { ReviewerCount } from '../stats/reviewer-count.model';

@Injectable({
  providedIn: 'root'
})
export class ReplacementService {
  baseUrl = `${environment.apiUrl}/replacements`;

  constructor(private httpClient: HttpClient) {}

  findNumReviewed(): Observable<number> {
    return this.httpClient.get<number>(`${this.baseUrl}/count?reviewed=true`);
  }

  findNumNotReviewed(): Observable<number> {
    return this.httpClient.get<number>(`${this.baseUrl}/count?reviewed=false`);
  }

  findNumReviewedByReviewer(): Observable<ReviewerCount[]> {
    return this.httpClient.get<ReviewerCount[]>(`${this.baseUrl}/count?reviewed=true&grouped`);
  }

  findReplacementCounts(): Observable<ReplacementCountList[]> {
    return this.httpClient.get<ReplacementCountList[]>(`${this.baseUrl}/count?reviewed=false&grouped`);
  }
}
