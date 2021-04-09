import { AccessToken } from '../authentication/access-token.model';

export interface User {
  name: string;
  hasRights: boolean;
  admin: boolean;
  accessToken: AccessToken;
}
