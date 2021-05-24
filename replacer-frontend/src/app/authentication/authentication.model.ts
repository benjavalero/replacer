export interface RequestToken {
  token: string;
  tokenSecret: string;
  authorizationUrl: string;
}

export class AuthenticateRequest {
  requestToken: string;
  requestTokenSecret: string;
  oauthVerifier: string;
}

export interface AuthenticateResponse {
  name: string;
  hasRights: boolean;
  admin: boolean;
  token: string;
  tokenSecret: string;
}
