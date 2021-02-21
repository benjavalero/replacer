import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ReplacementCountList } from './replacement-count-list.model';

@Injectable({
  providedIn: 'root'
})
export class ReplacementService {
  baseUrl = `${environment.apiUrl}/replacements`;

  constructor(private httpClient: HttpClient) {}

  findReplacementCounts(): Observable<ReplacementCountList[]> {
    return this.httpClient.get<ReplacementCountList[]>(`${this.baseUrl}/count?reviewed=false&grouped`);
  }
}
