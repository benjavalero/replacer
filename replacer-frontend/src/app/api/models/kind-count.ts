/* tslint:disable */
/* eslint-disable */
import { SubtypeCount } from './subtype-count';

/**
 * Count of pages to review grouped by kind
 */
export interface KindCount {

  /**
   * Replacement kind code
   */
  k: number;

  /**
   * Count of pages to review grouped by subtype for a given kind
   */
  l: Array<SubtypeCount>;
}
