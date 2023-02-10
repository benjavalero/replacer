/* tslint:disable */
/* eslint-disable */
import { ReviewSectionDto } from './review-section-dto';

/**
 * Page reviewed
 */
export interface ReviewPage {

  /**
   * Page (or section) content
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
  section?: ReviewSectionDto;

  /**
   * Page title
   */
  title: string;
}
