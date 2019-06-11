import { ReplacementSuggestion } from './replacement-suggestion.model';

export interface ArticleReplacement {
  text: string;
  start: number;
  type: string;
  subtype: string;
  suggestions: ReplacementSuggestion[];
}
