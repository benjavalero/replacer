import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, interval, Observable, Subscription } from 'rxjs';
import { environment } from '../../environments/environment';
import { ReplacementCount, ReplacementCountList } from './replacement-list.model';

@Injectable({
  providedIn: 'root'
})
export class ReplacementListService implements OnDestroy {
  private readonly countsKey = 'counts';
  readonly counts$ = new BehaviorSubject<ReplacementCountList[]>([]);

  // Task to refresh the counts periodically
  subscription: Subscription;

  constructor(private httpClient: HttpClient) {
    // Load replacement counts and initialize the scheduled task to refresh them
    // to avoid too many calls to the server
    this.loadCountsFromServer();
    this.subscription = interval(60000).subscribe(() => this.loadCountsFromServer());

    // In case the cached counts change in a different tab we capture the event
    // Note the event is not captured if the storage changes in the same tab
    window.addEventListener('storage', this.storageEventListener.bind(this));
  }

  ngOnDestroy() {
    this.counts$.complete();
    this.subscription.unsubscribe();

    window.removeEventListener('storage', this.storageEventListener.bind(this));
  }

  private storageEventListener(event: StorageEvent) {
    if (event.storageArea == localStorage && event.key === this.countsKey) {
      this.updateCounts(JSON.parse(event.newValue!));
    }
  }

  private loadCountsFromServer() {
    this.findReplacementCounts$().subscribe((typeCounts: ReplacementCountList[]) => this.updateCounts(typeCounts));
  }

  private findReplacementCounts$(): Observable<ReplacementCountList[]> {
    return this.httpClient.get<ReplacementCountList[]>(`${environment.apiUrl}/replacement-types/count`);
  }

  private updateCounts(counts: ReplacementCountList[]): void {
    localStorage.setItem(this.countsKey, JSON.stringify(counts));
    this.counts$.next(counts);
  }

  reviewPages$(type: string, subtype: string): Observable<any> {
    // Remove the type/subtype from the cache
    this.updateSubtypeCount(type, subtype, 0);

    let params: HttpParams = new HttpParams();
    params = params.append('type', type).append('subtype', subtype);
    return this.httpClient.post<any>(`${environment.apiUrl}/pages/review`, null, { params });
  }

  updateSubtypeCount(type: string, subtype: string, count: number): void {
    const currentCounts = JSON.parse(localStorage.getItem(this.countsKey)!);
    for (let i = 0; i < currentCounts.length; i++) {
      const typeCount: ReplacementCountList = currentCounts[i];
      if (typeCount.t === type) {
        for (let j = 0; j < typeCount.l.length; j++) {
          const subtypeCount: ReplacementCount = typeCount.l[j];
          if (subtypeCount.s === subtype) {
            subtypeCount.c = count;
            if (subtypeCount.c <= 0) {
              typeCount.l.splice(j, 1);
            }
          }
        }

        // In case the type becomes empty we remove it too
        if (typeCount.l.length == 0) {
          currentCounts.splice(i, 1);
        }
      }
    }
    this.updateCounts(currentCounts);
  }
}
