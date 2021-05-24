export class User {
  name: string;
  hasRights: boolean;
  admin: boolean;
  accessToken: AccessToken;
}

export class AccessToken {
  token: string;
  tokenSecret: string;
}
