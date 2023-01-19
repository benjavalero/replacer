/* tslint:disable */
/* eslint-disable */
import { RequestToken } from './request-token';

/**
 * Request token and authorization URL to initiate an authorization process
 */
export interface InitiateAuthenticationResponse {
  authorizationUrl: string;
  requestToken: RequestToken;
}
