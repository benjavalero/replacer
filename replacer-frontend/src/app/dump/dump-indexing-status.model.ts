export interface DumpIndexingStatus {
  running: boolean;
  numPagesRead?: number;
  numPagesProcessed?: number;
  numPagesEstimated?: number;
  dumpFileName?: string;
  start?: number;
  end?: number;
}
