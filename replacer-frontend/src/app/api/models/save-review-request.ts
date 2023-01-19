/* tslint:disable */
/* eslint-disable */
import { AccessToken } from './access-token';
import { ReviewPage } from './review-page';
import { ReviewedReplacement } from './reviewed-replacement';
export interface SaveReviewRequest {
  accessToken: AccessToken;
  page: ReviewPage;

  /**
   * Reviewed replacements
   */
  reviewedReplacements: Array<ReviewedReplacement>;
}
