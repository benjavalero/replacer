import { Language } from '../user/language-model';
import { AccessToken } from '../user/user.model';
import { ReviewReplacement } from './page-replacement.model';

export const kindLabel: { [key: number]: string } = {
  1: 'Personalizado',
  2: 'Ortografía',
  3: 'Compuestos',
  4: 'Fechas'
};

export class ReviewOptions {
  kind: number | null;
  subtype: string | null;
  suggestion: string | null;
  cs: boolean | null;

  constructor(kind: number | null, subtype: string | null, suggestion: string | null, cs: boolean | null) {
    this.kind = kind;
    this.subtype = subtype;
    this.suggestion = suggestion;
    this.cs = cs;
  }

  // TODO: Add constants or retrieve them from backend
  getKindLabel(): string | null {
    if (this.kind) {
      return kindLabel[this.kind];
    } else {
      return null;
    }
  }
}

export interface PageReviewResponse {
  page: ReviewPage;
  replacements: ReviewReplacement[];
  options: PageReviewOptions;
  numPending?: number;
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
  kind?: number;
  subtype?: string;
  suggestion?: string;
  cs?: boolean;
}

export class PageSaveRequest {
  page: ReviewPage;
  options: PageReviewOptions;
  reviewAllTypes: boolean;
  accessToken: AccessToken;

  constructor(page: ReviewPage, options: PageReviewOptions, reviewAllTypes: boolean, accessToken: AccessToken) {
    this.page = page;
    this.options = options;
    this.reviewAllTypes = reviewAllTypes;
    this.accessToken = accessToken;
  }
}
