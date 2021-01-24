import { AccessToken } from './access-token.model';

export interface WikipediaUser {
  name: string;
  admin: boolean;
  accessToken: AccessToken;
}

export enum Language {
  es = 'es',
  gl = 'gl'
}

export const LANG_PARAM = 'lang';
export const LANG_DEFAULT = Language.es;
