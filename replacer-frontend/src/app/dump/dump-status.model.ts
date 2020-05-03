export interface DumpStatus {
  running: boolean;
  numArticlesRead: number;
  numArticlesProcessed: number;
  numArticlesEstimated: number;
  dumpFileName: string;
  start: number;
  end?: number;
}
