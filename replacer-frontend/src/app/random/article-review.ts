import { ArticleReplacement } from './article-replacements';

export interface ArticleReview {
  title: string;
  content?: string;
  replacements?: ArticleReplacement[];
  trimText?: boolean;
}
