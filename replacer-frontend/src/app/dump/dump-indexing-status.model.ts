export interface DumpIndexingStatus {
  running: boolean;
  numArticlesRead: number;
  numArticlesProcessed: number;
  numArticlesEstimated: number;
  dumpFileName: string;
  start: number;
  end?: number;
}
