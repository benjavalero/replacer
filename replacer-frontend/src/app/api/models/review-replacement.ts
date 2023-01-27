/* tslint:disable */
/* eslint-disable */
import { ReviewSuggestion } from './review-suggestion';

/**
 * Replacement to review
 */
export interface ReviewReplacement {

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
  suggestions: Array<ReviewSuggestion>;

  /**
   * Text of the replacement
   */
  text: string;
}
