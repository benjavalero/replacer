/* tslint:disable */
/* eslint-disable */
import { Replacement } from '../models/replacement';
import { Section } from '../models/section';

/**
 * Page to review
 */
export interface Page {

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
  replacements: Array<Replacement>;
  section?: Section;

  /**
   * Page title
   */
  title: string;
}
