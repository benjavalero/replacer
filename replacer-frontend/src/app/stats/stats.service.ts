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
  private readonly baseUrl = `${environment.apiUrl}/replacement`;

  constructor(private httpClient: HttpClient) {}

  findNumReviewed$(): Observable<ReplacementCount> {
    return this.httpClient.get<ReplacementCount>(`${this.baseUrl}/count?reviewed=true`);
  }

  findNumNotReviewed$(): Observable<ReplacementCount> {
    return this.httpClient.get<ReplacementCount>(`${this.baseUrl}/count?reviewed=false`);
  }

  findNumReviewedByReviewer$(): Observable<ReviewerCount[]> {
    return this.httpClient.get<ReviewerCount[]>(`${this.baseUrl}/user/count`);
  }
}
