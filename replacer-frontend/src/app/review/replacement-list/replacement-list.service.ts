import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { KindCount } from '../../api/models/kind-count';
import { PagesService } from '../../api/services/pages.service';

@Injectable()
export class ReplacementListService {
  readonly counts$ = new BehaviorSubject<KindCount[] | null>(null);

  constructor(private pagesService: PagesService) {
    // Service don't implement OnInit so all initialization must be done in the constructor
  }

  loadCountsFromServer(): void {
    this.findReplacementCounts$().subscribe((typeCounts: KindCount[]) => this.updateCounts(typeCounts));
  }

  private findReplacementCounts$(): Observable<KindCount[]> {
    return this.pagesService.countPagesNotReviewedByType();
  }

  private updateCounts(counts: KindCount[]): void {
    this.counts$.next(counts);
  }

  reviewSubtype$(kind: number, subtype: string): Observable<void> {
    // Remove the kind/subtype from the cache
    this.updateSubtypeCount(kind, subtype, 0);

    return this.pagesService.reviewPagesByType({
      kind: kind,
      subtype: subtype
    });
  }

  private updateSubtypeCount(kind: number, subtype: string, count: number): void {
    const currentCounts = this.counts$.getValue();
    if (!currentCounts) {
      console.warn('No replacement counts initialized yet');
      return;
    }

    for (let typeCount of currentCounts) {
      if (typeCount.k === kind) {
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
