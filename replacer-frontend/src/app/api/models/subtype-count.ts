/* tslint:disable */
/* eslint-disable */

/**
 * Count of pages to review grouped by subtype
 */
export interface SubtypeCount {

  /**
   * Count of pages to review containing this subtype
   */
  c: number;
  forAdmin?: boolean;
  forBots?: boolean;

  /**
   * Replacement subtype
   */
  s: string;
}
