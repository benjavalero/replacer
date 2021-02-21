import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ReviewerCount } from './reviewer-count.model';

@Injectable({
  providedIn: 'root'
})
export class StatsService {
  private readonly baseUrl = `${environment.apiUrl}/replacements`;

  constructor(private httpClient: HttpClient) {}

  findNumReviewed$(): Observable<number> {
    return this.httpClient.get<number>(`${this.baseUrl}/count?reviewed=true`);
  }

  findNumNotReviewed$(): Observable<number> {
    return this.httpClient.get<number>(`${this.baseUrl}/count?reviewed=false`);
  }

  findNumReviewedByReviewer$(): Observable<ReviewerCount[]> {
    return this.httpClient.get<ReviewerCount[]>(`${this.baseUrl}/count?reviewed=true&grouped`);
  }
}
