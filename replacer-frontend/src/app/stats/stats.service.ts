import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ReplacementCount } from './count.model';
import { ReviewerCount } from './reviewer-count.model';

@Injectable({
  providedIn: 'root'
})
export class StatsService {
  constructor(private httpClient: HttpClient) {}

  findNumReviewed$(): Observable<ReplacementCount> {
    return this.httpClient.get<ReplacementCount>(`${environment.apiUrl}/replacements/count?reviewed=true`);
  }

  findNumNotReviewed$(): Observable<ReplacementCount> {
    return this.httpClient.get<ReplacementCount>(`${environment.apiUrl}/replacements/count?reviewed=false`);
  }

  findNumReviewedByReviewer$(): Observable<ReviewerCount[]> {
    return this.httpClient.get<ReviewerCount[]>(`${environment.apiUrl}/users/count`);
  }
}
