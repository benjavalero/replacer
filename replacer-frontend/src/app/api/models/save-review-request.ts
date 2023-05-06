/* tslint:disable */
/* eslint-disable */
import { ReviewPage } from './review-page';
import { ReviewedReplacement } from './reviewed-replacement';
export interface SaveReviewRequest {
  page: ReviewPage;

  /**
   * Reviewed replacements
   */
  reviewedReplacements: Array<ReviewedReplacement>;
}
