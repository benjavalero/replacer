/* tslint:disable */
/* eslint-disable */
import { Language } from './language';
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
   * Page ID
   */
  id: number;
  lang: Language;

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
