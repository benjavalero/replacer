export interface DumpStatus {
  running: boolean;
  numArticlesRead: number;
  numArticlesProcessed: number;
  dumpFileName: string;
  start: number;
  end?: number;
}
