/* tslint:disable */
/* eslint-disable */
import { Suggestion } from './suggestion';

/**
 * Replacement to review
 */
export interface Replacement {

  /**
   * If the replacement is case-sensitive. Only for custom replacements.
   */
  cs?: boolean;

  /**
   * Kind of the replacement
   */
  kind: number;

  /**
   * Position of the replacement in the content
   */
  start: number;

  /**
   * Subtype of the replacement
   */
  subtype: string;

  /**
   * Collection of suggestions to fix the replacement
   */
  suggestions: Array<Suggestion>;

  /**
   * Text of the replacement
   */
  text: string;
}
