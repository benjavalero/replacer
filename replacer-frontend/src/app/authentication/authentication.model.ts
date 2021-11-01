export interface RequestToken {
  token: string;
  tokenSecret: string;
  authorizationUrl: string;
}

export class AuthenticateRequest {
  token: string;
  tokenSecret: string;
  oauthVerifier: string;

  constructor(requestToken: RequestToken, oauthVerifier: string) {
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
  token: string;
  tokenSecret: string;
}
