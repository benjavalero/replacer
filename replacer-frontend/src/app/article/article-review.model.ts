import { ArticleReplacement } from './article-replacement.model';

export interface ArticleReview {
  id: number;
  title: string;
  content: string;
  currentTimestamp: string;
  replacements: ArticleReplacement[];
}
