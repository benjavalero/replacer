/* tslint:disable */
/* eslint-disable */

/**
 * Authenticated application user
 */
export interface User {

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

  /**
   * If the user is a special user, e.g. a patroller
   */
  specialUser: boolean;
}
