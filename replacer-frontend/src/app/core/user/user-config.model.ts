export enum Language {
  SPANISH = 'es',
  GALICIAN = 'gl'
}

export class UserConfig {
  lang: Language;

  constructor(lang: Language) {
    this.lang = lang;
  }
}
