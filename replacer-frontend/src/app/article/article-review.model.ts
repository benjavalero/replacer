import { ArticleReplacement } from './article-replacement.model';

export interface ArticleReview {
  id: number;
  title: string;
  content: string;
  replacements: ArticleReplacement[];
  trimText: boolean;
}
