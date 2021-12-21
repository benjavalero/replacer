import { AccessToken } from '../user/user.model';

export interface RequestTokenResponse {
  token: string;
  tokenSecret: string;
  authorizationUrl: string;
}

export class AuthenticateRequest {
  token: string;
  tokenSecret: string;
  oauthVerifier: string;

  constructor(requestToken: RequestTokenResponse, oauthVerifier: string) {
    this.token = requestToken.token;
    this.tokenSecret = requestToken.tokenSecret;
    this.oauthVerifier = oauthVerifier;
  }
}

export interface AuthenticateResponse {
  name: string;
  hasRights: boolean;
  bot: boolean;
  admin: boolean;
  accessToken: AccessToken;
}
