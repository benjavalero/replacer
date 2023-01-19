/* tslint:disable */
/* eslint-disable */

/**
 * Options of the replacements to review
 */
export interface ReviewOptions {

  /**
   * If the custom replacement is case-sensitive
   */
  cs?: boolean;

  /**
   * Replacement kind code
   */
  kind?: number;

  /**
   * Replacement subtype
   */
  subtype?: string;

  /**
   * Custom replacement suggestion
   */
  suggestion?: string;
}
