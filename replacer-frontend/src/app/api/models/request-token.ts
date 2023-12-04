/* tslint:disable */
/* eslint-disable */

/**
 * Request token with an authorization URL to initiate an authorization process
 */
export interface RequestToken {
  authorizationUrl: string;
  token: string;
  tokenSecret: string;
}
