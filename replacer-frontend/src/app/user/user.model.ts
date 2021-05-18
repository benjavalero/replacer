export interface User {
  name: string;
  hasRights: boolean;
  admin: boolean;
  token: string;
  tokenSecret: string;
}
