import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Language } from './language-model';
import { UserConfig } from './user-config.model';

export const LANG_PARAM = 'lang';

@Injectable({
  providedIn: 'root'
})
export class UserConfigService {
  private readonly LANG_DEFAULT: Language = Language.es;

  private readonly userConfigKey = 'userConfig';
  private readonly _config = new BehaviorSubject<UserConfig>(this.emptyConfig());

  constructor() {
    this.loadConfig();
  }

  get config$(): Observable<UserConfig> {
    return this._config.asObservable();
  }

  private loadConfig(): void {
    // TODO: We clean the old lang key. This line must be removed in the future.
    localStorage.removeItem('lang');

    let config = JSON.parse(localStorage.getItem(this.userConfigKey));
    if (!this.isValid(config)) {
      config = this.emptyConfig();
    }

    this._config.next(config);
  }

  private emptyConfig(): UserConfig {
    return { lang: this.LANG_DEFAULT };
  }

  private isValid(config: UserConfig): boolean {
    return config != null && config.lang != null;
  }

  get lang(): Language {
    return this._config.getValue().lang;
  }

  set lang(lang: Language) {
    const currentConfig = this._config.getValue();
    const newConfig = {...currentConfig, lang: lang};

    localStorage.setItem(this.userConfigKey, JSON.stringify(newConfig));
    this._config.next(newConfig);
  }
}
