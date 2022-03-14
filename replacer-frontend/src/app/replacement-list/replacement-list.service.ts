import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { KindCount } from './replacement-list.model';

@Injectable({
  providedIn: 'root'
})
export class ReplacementListService {
  readonly counts$ = new BehaviorSubject<KindCount[] | null>(null);

  constructor(private httpClient: HttpClient) {
    // Service don't implement OnInit so all initialization must be done in the constructor
  }

  loadCountsFromServer(): void {
    this.findReplacementCounts$().subscribe((typeCounts: KindCount[]) => this.updateCounts(typeCounts));
  }

  private findReplacementCounts$(): Observable<KindCount[]> {
    return this.httpClient.get<KindCount[]>(`${environment.apiUrl}/page/type/count`);
  }

  private updateCounts(counts: KindCount[]): void {
    this.counts$.next(counts);
  }

  reviewSubtype$(type: number, subtype: string): Observable<void> {
    // Remove the type/subtype from the cache
    this.updateSubtypeCount(type, subtype, 0);

    let params: HttpParams = new HttpParams();
    params = params.append('type', type).append('subtype', subtype);
    return this.httpClient.post<void>(`${environment.apiUrl}/pages/review`, null, { params });
  }

  private updateSubtypeCount(type: number, subtype: string, count: number): void {
    const currentCounts = this.counts$.getValue();
    if (!currentCounts) {
      console.warn('No replacement counts initialized yet');
      return;
    }

    for (let typeCount of currentCounts) {
      if (typeCount.k === type) {
        for (let subtypeCount of typeCount.l) {
          if (subtypeCount.s === subtype) {
            subtypeCount.c = count;
          }
        }
      }
    }
    this.updateCounts(currentCounts);
  }
}
