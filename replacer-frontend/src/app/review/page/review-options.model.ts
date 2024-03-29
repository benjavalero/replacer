/**
 * Options of the replacements to review
 */
export interface ReviewOptions {
  /**
   * Replacement kind code
   */
  kind?: number;

  /**
   * Replacement subtype
   */
  subtype?: string;

  /**
   * If the custom replacement is case-sensitive
   */
  cs?: boolean;

  /**
   * Custom replacement suggestion
   */
  suggestion?: string;
}
