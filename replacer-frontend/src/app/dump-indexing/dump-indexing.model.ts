export interface DumpIndexing {
  // Fields coming from REST service
  running: boolean;
  numPagesRead?: number;
  numPagesProcessed?: number;
  numPagesEstimated?: number;
  dumpFileName?: string;
  start?: number;
  end?: number;

  // Fields calculated
  startDate?: Date;
  endDate?: Date;
  elapsed?: string;
  progress?: number;
  average?: number;
  eta?: string;
}
