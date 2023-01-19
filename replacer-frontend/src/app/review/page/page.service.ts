import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { FindReviewResponse } from '../../api/models/find-review-response';
import { ReplacementType } from '../../api/models/replacement-type';
import { ReviewOptions } from '../../api/models/review-options';
import { ReviewPage } from '../../api/models/review-page';
import { ReviewedReplacement } from '../../api/models/reviewed-replacement';
import { SaveReviewRequest } from '../../api/models/save-review-request';
import { ReplacementsService } from '../../api/services/replacements.service';
import { ReviewService } from '../../api/services/review.service';
import { UserService } from '../../core/user/user.service';

export const EMPTY_CONTENT = ' ';

@Injectable()
export class PageService {
  constructor(
    private reviewService: ReviewService,
    private userService: UserService,
    private replacementsService: ReplacementsService
  ) {}

  findRandomPage(options: ReviewOptions): Observable<FindReviewResponse> {
    return this.reviewService.findRandomPageWithReplacements({ ...options });
  }

  validateCustomReplacement(replacement: string, caseSensitive: boolean): Observable<ReplacementType> {
    return this.replacementsService.validateCustomReplacement({
      replacement: replacement,
      cs: caseSensitive
    });
  }

  findPageReviewById(pageId: number, options: ReviewOptions): Observable<FindReviewResponse> {
    return this.reviewService.findPageReviewById({ ...options, id: pageId });
  }

  saveReview(page: ReviewPage, reviewedReplacements: ReviewedReplacement[]): Observable<void> {
    if (!this.userService.isValidUser()) {
      return throwError(() => new Error('El usuario no está autenticado. Recargue la página para retomar la sesión.'));
    }

    const saveReview = {
      page: page,
      reviewedReplacements: reviewedReplacements,
      accessToken: this.userService.accessToken
    } as SaveReviewRequest;

    // Call backend and delay the observable response
    return this.reviewService.saveReview({
      id: page.id,
      body: saveReview
    });
  }
}
