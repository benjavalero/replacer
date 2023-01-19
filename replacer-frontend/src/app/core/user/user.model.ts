import { AccessToken } from '../../api/models/access-token';
import { VerifyAuthenticationResponse } from '../../api/models/verify-authentication-response';

export class User {
  name: string;
  hasRights: boolean;
  bot: boolean;
  admin: boolean;
  accessToken: AccessToken;

  // TODO: Decouple this from Authentication feature
  constructor(response: VerifyAuthenticationResponse) {
    this.name = response.name;
    this.hasRights = response.hasRights;
    this.bot = response.bot;
    this.admin = response.admin;
    this.accessToken = response.accessToken;
  }
}
