import { Language } from '../user/language-model';
import { ReviewReplacement } from './page-replacement.model';

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

export interface PageReviewResponse {
  page: ReviewPage;
  replacements: ReviewReplacement[];
  search: PageReviewSearch;
  numPending: number;
}

export interface ReviewPage {
  lang: Language;
  id: number;
  title: string;
  content: string;
  section?: ReviewSection;
  queryTimestamp: string;
}

interface ReviewSection {
  id: number;
  title: string;
}

export interface PageReviewSearch {
  type?: string;
  subtype?: string;
  suggestion?: string;
  cs?: boolean;
}

export class PageSaveRequest {
  page: ReviewPage;
  search: PageReviewSearch;
  token: string;
  tokenSecret: string;

  constructor(page: ReviewPage, search: PageReviewSearch, token: string, tokenSecret: string) {
    this.page = page;
    this.search = search;
    this.token = token;
    this.tokenSecret = tokenSecret;
  }
}
