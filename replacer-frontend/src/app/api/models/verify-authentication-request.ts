/* tslint:disable */
/* eslint-disable */
import { RequestToken } from './request-token';

/**
 * Request token and OAuth verifier to complete the authorization process
 */
export interface VerifyAuthenticationRequest {
  oauthVerifier: string;
  requestToken: RequestToken;
}
