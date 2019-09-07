import { ArticleReplacement } from './article-replacement.model';

export interface ArticleReview {
  articleId: number;
  title: string;
  content: string;
  section?: number;
  currentTimestamp: string;
  replacements: ArticleReplacement[];
}
