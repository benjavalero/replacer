import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ReplacementCountList } from './replacement-list.model';

@Injectable({
  providedIn: 'root'
})
export class ReplacementListService {
  private readonly _counts = new BehaviorSubject<ReplacementCountList[]>(null);

  get counts$(): Observable<ReplacementCountList[]> {
    return this._counts.asObservable();
  }

  constructor(private httpClient: HttpClient) {}

  loadCountsFromServer() {
    this.findReplacementCounts$().subscribe((typeCounts: ReplacementCountList[]) => this._counts.next(typeCounts));
  }

  private findReplacementCounts$(): Observable<ReplacementCountList[]> {
    return this.httpClient.get<ReplacementCountList[]>(`${environment.apiUrl}/replacement-types/count`);
  }

  reviewPages$(type: string, subtype: string): Observable<any> {
    let params: HttpParams = new HttpParams();
    params = params.append('type', type).append('subtype', subtype);
    return this.httpClient.post<any>(`${environment.apiUrl}/pages/review`, null, { params });
  }
}
