import { AuthenticateResponse } from '../authentication/authentication.model';

export class User {
  name: string;
  hasRights: boolean;
  bot: boolean;
  admin: boolean;
  accessToken: AccessToken;

  constructor(response: AuthenticateResponse) {
    this.name = response.name;
    this.hasRights = response.hasRights;
    this.bot = response.bot;
    this.admin = response.admin;
    this.accessToken = response.accessToken;
  }
}

export interface AccessToken {
  token: string;
  tokenSecret: string;
}
