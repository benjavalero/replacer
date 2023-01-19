/* tslint:disable */
/* eslint-disable */
import { AccessToken } from './access-token';

/**
 * Application user with access token after completing the authorization verification
 */
export interface VerifyAuthenticationResponse {
  accessToken: AccessToken;

  /**
   * If the user is administrator of Replacer
   */
  admin: boolean;

  /**
   * If the user is a bot
   */
  bot: boolean;

  /**
   * If the user the rights to use the tool
   */
  hasRights: boolean;

  /**
   * Name of the user in Wikipedia
   */
  name: string;
}
