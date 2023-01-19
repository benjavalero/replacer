/* tslint:disable */
/* eslint-disable */

/**
 * Reviewed replacement
 */
export interface ReviewedReplacement {

  /**
   * If the replacement is case-sensitive. Only for custom replacements.
   */
  cs?: boolean;

  /**
   * True if fixed. False if reviewed with no changes.
   */
  fixed: boolean;

  /**
   * Replacement kind
   */
  kind: number;

  /**
   * Replacement start position
   */
  start: number;

  /**
   * Replacement subtype
   */
  subtype: string;
}
