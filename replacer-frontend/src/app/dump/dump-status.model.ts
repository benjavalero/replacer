export interface DumpStatus {
  running: boolean;
  forceProcess: boolean;
  numArticlesRead: number;
  numArticlesProcessed: number;
  dumpFileName: string;
  start?: number;
  end?: number;
}
