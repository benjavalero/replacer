export interface DumpStatus {
  running: boolean;
  forceProcess: boolean;
  numArticlesRead: number;
  numArticlesProcessable: number;
  numArticlesProcessed: number;
  dumpFileName: string;
  start?: number;
  end?: number;
}
