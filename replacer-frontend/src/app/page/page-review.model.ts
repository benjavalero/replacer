import { AccessToken } from '../authentication/access-token.model';
import { Language } from '../user/language-model';
import { PageReplacement } from './page-replacement.model';

export interface PageReview {
  page: PageDto;
  replacements: PageReplacement[];
  search: PageSearch;
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

export interface PageSearch {
  numPending: number;
  type?: string;
  subtype?: string;
  suggestion?: string;
  cs?: boolean;
}

export class SavePage {
  page: PageDto;
  search: PageSearch;
  accessToken: AccessToken;
}
