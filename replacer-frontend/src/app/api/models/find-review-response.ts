/* tslint:disable */
/* eslint-disable */
import { ReviewPage } from './review-page';
import { ReviewReplacement } from './review-replacement';

/**
 * Page and replacements to review
 */
export interface FindReviewResponse {

  /**
   * Number of pending pages to review of the given type
   */
  numPending: number;
  page: ReviewPage;

  /**
   * Collection of replacements to review
   */
  replacements: Array<ReviewReplacement>;
}
