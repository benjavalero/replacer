import { AccessToken } from '../user/user.model';

export interface InitiateAuthenticationResponse {
  requestToken: RequestToken;
  authorizationUrl: string;
}

export interface RequestToken {
  token: string;
  tokenSecret: string;
}

export class VerifyAuthenticationRequest {
  requestToken: RequestToken;
  oauthVerifier: string;

  constructor(requestToken: RequestToken, oauthVerifier: string) {
    this.requestToken = requestToken;
    this.oauthVerifier = oauthVerifier;
  }
}

export interface VerifyAuthenticationResponse {
  name: string;
  hasRights: boolean;
  bot: boolean;
  admin: boolean;
  accessToken: AccessToken;
}
