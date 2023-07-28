/* tslint:disable */
/* eslint-disable */
import { RequestToken } from './request-token';

/**
 * Request token and OAuth verifier to complete the authorization process
 */
export interface VerifyAuthorizationRequest {
  oauthVerifier: string;
  requestToken: RequestToken;
}
