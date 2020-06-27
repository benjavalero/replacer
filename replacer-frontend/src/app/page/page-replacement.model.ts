import { Suggestion } from './suggestion.model';

export interface PageReplacement {
  text: string;
  start: number;
  suggestions: Suggestion[];
  textFixed?: string;
}
