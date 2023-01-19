import { Language } from '../../api/models/language';

export class UserConfig {
  lang: Language;

  constructor(lang: Language) {
    this.lang = lang;
  }
}
