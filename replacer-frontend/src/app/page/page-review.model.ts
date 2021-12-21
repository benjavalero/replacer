import { Language } from '../user/language-model';
import { AccessToken } from '../user/user.model';
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
  options: PageReviewOptions;
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

export interface PageReviewOptions {
  type?: string;
  subtype?: string;
  suggestion?: string;
  cs?: boolean;
}

export class PageSaveRequest {
  page: ReviewPage;
  options: PageReviewOptions;
  accessToken: AccessToken;

  constructor(page: ReviewPage, options: PageReviewOptions, accessToken: AccessToken) {
    this.page = page;
    this.options = options;
    this.accessToken = accessToken;
  }
}
