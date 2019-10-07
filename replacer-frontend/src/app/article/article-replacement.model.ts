import { Suggestion } from './suggestion.model';

export interface ArticleReplacement {
  text: string;
  start: number;
  suggestions: Suggestion[];
  textFixed?: string;
}
