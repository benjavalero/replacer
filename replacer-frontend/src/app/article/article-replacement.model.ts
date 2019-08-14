import { ReplacementSuggestion } from './replacement-suggestion.model';

export interface ArticleReplacement {
  text: string;
  start: number;
  suggestions: ReplacementSuggestion[];
  textFixed?: string;
}
