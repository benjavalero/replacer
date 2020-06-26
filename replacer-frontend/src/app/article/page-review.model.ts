import { ArticleReplacement } from './article-replacement.model';

export interface PageReview {
  id: number;
  title: string;
  content: string;
  section?: number;
  queryTimestamp: string;
  replacements: ArticleReplacement[];
  numPending: number;
}
