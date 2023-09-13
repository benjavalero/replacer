/* tslint:disable */
/* eslint-disable */
import { ReviewedReplacement } from './reviewed-replacement';

/**
 * Reviewed page. The page fields are only mandatory when saving the page with changes.
 */
export interface ReviewedPage {

  /**
   * Page (or section) content
   */
  content?: string;

  /**
   * Timestamp when the page content was retrieved from Wikipedia
   */
  queryTimestamp?: string;

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

  /**
   * Page title
   */
  title?: string;
}
