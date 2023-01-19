/* tslint:disable */
/* eslint-disable */

/**
 * Section of a page to review
 */
export interface ReviewSectionDto {

  /**
   * Section ID
   */
  id: number;

  /**
   * Offset of the section with the page content
   */
  offset: number;

  /**
   * Section title
   */
  title: string;
}
