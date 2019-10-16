import { AccessToken } from '../authentication/access-token.model';

export class SaveArticle {
  articleId: number;
  section?: number;
  content: string;
  timestamp: string;
  reviewer: string;
  token: AccessToken;
  type?: string;
  subtype?: string;
}
