import { AccessToken } from '../authentication/access-token.model';

export class SavePage {
  section?: number;
  title: string;
  content: string;
  timestamp: string;
  token: AccessToken;
  type?: string;
  subtype?: string;
}
