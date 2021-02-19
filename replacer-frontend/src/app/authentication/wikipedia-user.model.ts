import { AccessToken } from './access-token.model';

export interface WikipediaUser {
  name: string;
  admin: boolean;
  accessToken: AccessToken;
}
