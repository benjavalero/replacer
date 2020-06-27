import { PageReplacement } from './page-replacement.model';

export interface PageReview {
  id: number;
  title: string;
  content: string;
  section?: number;
  queryTimestamp: string;
  replacements: PageReplacement[];
  numPending: number;
}
