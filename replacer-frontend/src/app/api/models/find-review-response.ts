/* tslint:disable */
/* eslint-disable */
import { ReviewOptions } from './review-options';
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
  options: ReviewOptions;
  page: ReviewPage;

  /**
   * Collection of replacements to review
   */
  replacements: Array<ReviewReplacement>;
}
