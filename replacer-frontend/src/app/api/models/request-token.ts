/* tslint:disable */
/* eslint-disable */

/**
 * Request token to initiate an authorization process
 */
export interface RequestToken {
  authorizationUrl: string;
  token: string;
  tokenSecret: string;
}
