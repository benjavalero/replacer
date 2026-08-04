import { inject, Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { KindCount } from '../../api/models/kind-count';
import { PageApiService } from '../../api/services/page-api.service';

@Injectable()
export class ReplacementListService {
  private pageApiService = inject(PageApiService);

  readonly counts = signal<KindCount[] | null>(null);

  loadCountsFromServer(): void {
    this.pageApiService.countNotReviewedGroupedByType().subscribe((kindCounts) => this.counts.set(kindCounts));
  }

  reviewSubtype$(kind: number, subtype: string): Observable<void> {
    // Remove the kind/subtype from the cache
    this.updateSubtypeCount(kind, subtype, 0);

    return this.pageApiService.reviewPagesByType({ kind, subtype });
  }

  private updateSubtypeCount(kind: number, subtype: string, count: number): void {
    const currentCounts = this.counts();
    if (currentCounts === null) {
      console.warn('No replacement counts initialized yet');
      return;
    }

    for (const kindCount of currentCounts) {
      if (kindCount.k === kind) {
        for (const subtypeCount of kindCount.l) {
          if (subtypeCount.s === subtype) {
            subtypeCount.c = count;
          }
        }
      }
    }
    this.counts.set([...currentCounts]);
  }
}
