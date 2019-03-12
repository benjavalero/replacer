export interface DumpStatus {
  running: boolean;
  forceProcess: boolean;
  numArticlesRead: number;
  numArticlesProcessed: number;
  dumpFileName: string;
  average: number;
  time: string;
  progress: string;
}
