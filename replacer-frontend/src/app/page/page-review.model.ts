import { Language } from '../user/language-model';
import { PageReplacement } from './page-replacement.model';

export class ReviewOptions {
  type: string | null;
  subtype: string | null;
  suggestion: string | null;
  cs: boolean | null;

  constructor(type: string | null, subtype: string | null, suggestion: string | null, cs: boolean | null) {
    this.type = type;
    this.subtype = subtype;
    this.suggestion = suggestion;
    this.cs = cs;
  }
}

export interface PageReview {
  page: PageDto;
  replacements: PageReplacement[];
  search: PageReviewSearch;
  numPending: number;
}

export interface PageDto {
  lang: Language;
  id: number;
  title: string;
  content: string;
  section?: PageSection;
  queryTimestamp: string;
}

interface PageSection {
  id: number;
  title: string;
}

export interface PageReviewSearch {
  type?: string;
  subtype?: string;
  suggestion?: string;
  cs?: boolean;
}

export class SavePage {
  page: PageDto;
  search: PageReviewSearch;
  token: string;
  tokenSecret: string;

  constructor(page: PageDto, search: PageReviewSearch, token: string, tokenSecret: string) {
    this.page = page;
    this.search = search;
    this.token = token;
    this.tokenSecret = tokenSecret;
  }
}
