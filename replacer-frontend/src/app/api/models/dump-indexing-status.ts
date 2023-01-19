/* tslint:disable */
/* eslint-disable */

/**
 * Status of the current (or the last) dump indexing
 */
export interface DumpIndexingStatus {

  /**
   * Filename of the indexed dump
   */
  dumpFileName?: string;

  /**
   * Indexing end time (in ms)
   */
  end?: number;

  /**
   * Estimated number of indexable pages
   */
  numPagesEstimated?: number;

  /**
   * Number of indexable pages indexed
   */
  numPagesIndexed?: number;

  /**
   * Number of indexable pages read
   */
  numPagesRead?: number;

  /**
   * If the indexing is running
   */
  running: boolean;

  /**
   * Indexing start time (in ms)
   */
  start?: number;
}
