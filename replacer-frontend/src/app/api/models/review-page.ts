/* tslint:disable */
/* eslint-disable */
import { ReviewReplacement } from './review-replacement';
import { ReviewSection } from './review-section';

/**
 * Page to review
 */
export interface ReviewPage {

  /**
   * Page (or section) content. When saving without changes, it matches a string with an only whitespace.
   */
  content: string;

  /**
   * Language of the Wikipedia in use
   */
  lang: string;

  /**
   * Page ID
   */
  pageId: number;

  /**
   * Timestamp when the page content was retrieved from Wikipedia
   */
  queryTimestamp: string;

  /**
   * Collection of replacements to review
   */
  replacements: Array<ReviewReplacement>;
  section?: ReviewSection;

  /**
   * Page title
   */
  title: string;
}
