export class User {
  name: string;
  hasRights: boolean;
  bot: boolean;
  admin: boolean;
  accessToken: AccessToken;
}

export class AccessToken {
  token: string;
  tokenSecret: string;
}
