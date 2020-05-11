export interface WikipediaUser {
  name: string;
  admin: boolean;
  lang: Language;
}

export enum Language {
  es = 'es',
  gl = 'gl'
}

export const LANG_PARAM = 'lang';
