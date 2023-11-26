import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { KindCount } from '../../api/models/kind-count';
import { PageApiService } from '../../api/services/page-api.service';

@Injectable()
export class ReplacementListService {
  readonly counts$ = new BehaviorSubject<KindCount[] | null>(null);

  constructor(private pageApiService: PageApiService) {
    // Service don't implement OnInit so all initialization must be done in the constructor
  }

  loadCountsFromServer(): void {
    this.findReplacementCounts$().subscribe((kindCounts: KindCount[]) => this.updateCounts(kindCounts));
  }

  private findReplacementCounts$(): Observable<KindCount[]> {
    return this.pageApiService.countNotReviewedGroupedByType();
  }

  private updateCounts(counts: KindCount[]): void {
    this.counts$.next(counts);
  }

  reviewSubtype$(kind: number, subtype: string): Observable<void> {
    // Remove the kind/subtype from the cache
    this.updateSubtypeCount(kind, subtype, 0);

    return this.pageApiService.reviewPagesByType({
      kind: kind,
      subtype: subtype
    });
  }

  private updateSubtypeCount(kind: number, subtype: string, count: number): void {
    const currentCounts = this.counts$.getValue();
    if (currentCounts === null) {
      console.warn('No replacement counts initialized yet');
      return;
    }

    for (let kindCount of currentCounts) {
      if (kindCount.k === kind) {
        for (let subtypeCount of kindCount.l) {
          if (subtypeCount.s === subtype) {
            subtypeCount.c = count;
          }
        }
      }
    }
    this.updateCounts(currentCounts);
  }
}
