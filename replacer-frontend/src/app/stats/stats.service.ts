import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ReplacementCount } from '../api/models/replacement-count';
import { ReviewerCount } from '../api/models/reviewer-count';
import { ReplacementsService } from '../api/services/replacements.service';

@Injectable()
export class StatsService {
  constructor(private replacementService: ReplacementsService) {}

  findNumReviewed$(): Observable<ReplacementCount> {
    return this.replacementService.countReplacements({
      reviewed: true
    });
  }

  findNumNotReviewed$(): Observable<ReplacementCount> {
    return this.replacementService.countReplacements({
      reviewed: false
    });
  }

  findNumReviewedByReviewer$(): Observable<ReviewerCount[]> {
    return this.replacementService.countReplacementsGroupedByReviewer();
  }
}
