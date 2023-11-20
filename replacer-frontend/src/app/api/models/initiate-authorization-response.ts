/* tslint:disable */
/* eslint-disable */
import { RequestToken } from '../models/request-token';

/**
 * Request token and authorization URL to initiate an authorization process
 */
export interface InitiateAuthorizationResponse {
  authorizationUrl: string;
  requestToken: RequestToken;
}
