import { Language } from './language.model';

export class UserConfig {
  lang: Language;

  constructor(lang: Language) {
    this.lang = lang;
  }
}
