import { AccessToken } from '../../api/models/access-token';
export interface User {
  name: string;
  hasRights: boolean;
  bot: boolean;
  admin: boolean;
  accessToken: AccessToken;
}
