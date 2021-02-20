import { AccessToken } from '../authentication/access-token.model';

export interface User {
  name: string;
  admin: boolean;
  accessToken: AccessToken;
}
