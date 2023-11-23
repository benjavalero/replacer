import { Language } from '../model/language.model';

export class UserConfig {
  lang: Language;

  constructor(lang: Language) {
    this.lang = lang;
  }
}
