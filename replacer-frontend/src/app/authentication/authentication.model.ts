export interface RequestToken {
  token: string;
  tokenSecret: string;
  authorizationUrl: string;
}

export class AuthenticateRequest {
  requestToken: string;
  requestTokenSecret: string;
  oauthVerifier: string;

  constructor(requestToken: RequestToken, oauthVerifier: string) {
    this.requestToken = requestToken.token;
    this.requestTokenSecret = requestToken.tokenSecret;
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
