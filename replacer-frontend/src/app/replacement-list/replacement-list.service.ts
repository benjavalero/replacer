import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TypeCount } from './replacement-list.model';

@Injectable({
  providedIn: 'root'
})
export class ReplacementListService {
  private readonly countsKey = 'counts';
  readonly counts$ = new BehaviorSubject<TypeCount[] | null>(null);

  constructor(private httpClient: HttpClient) {
    // Service don't implement OnInit so all initialization must be done in the constructor
    // TODO: "counts" property is obsolete
    if (localStorage.getItem(this.countsKey)) {
      localStorage.removeItem(this.countsKey);
    }
  }

  loadCountsFromServer(): void {
    this.findReplacementCounts$().subscribe((typeCounts: TypeCount[]) => this.updateCounts(typeCounts));
  }

  private findReplacementCounts$(): Observable<TypeCount[]> {
    return this.httpClient.get<TypeCount[]>(`${environment.apiUrl}/replacement-types/count`);
  }

  private updateCounts(counts: TypeCount[]): void {
    this.counts$.next(counts);
  }

  reviewSubtype$(type: string, subtype: string): Observable<void> {
    // Remove the type/subtype from the cache
    this.updateSubtypeCount(type, subtype, 0);

    let params: HttpParams = new HttpParams();
    params = params.append('type', type).append('subtype', subtype);
    return this.httpClient.post<void>(`${environment.apiUrl}/pages/review`, null, { params });
  }

  private updateSubtypeCount(type: string, subtype: string, count: number): void {
    const currentCounts = this.counts$.getValue();
    if (!currentCounts) {
      console.warn('No replacement counts initialized yet');
      return;
    }

    for (let typeCount of currentCounts) {
      if (typeCount.t === type) {
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
