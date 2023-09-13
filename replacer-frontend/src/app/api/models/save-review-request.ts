/* tslint:disable */
/* eslint-disable */
import { ReviewedReplacement } from './reviewed-replacement';
export interface SaveReviewRequest {

  /**
   * Page (or section) content. When saving without changes, it matches a string with an only whitespace.
   */
  content: string;

  /**
   * Timestamp when the page content was retrieved from Wikipedia
   */
  queryTimestamp: string;

  /**
   * Reviewed replacements
   */
  reviewedReplacements: Array<ReviewedReplacement>;

  /**
   * Section ID
   */
  sectionId?: number;

  /**
   * Offset of the section with the page content
   */
  sectionOffset?: number;
}
