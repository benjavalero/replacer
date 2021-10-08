import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Language } from './language-model';
import { UserConfig } from './user-config.model';

@Injectable({
  providedIn: 'root'
})
export class UserConfigService {
  private readonly LANG_DEFAULT: Language = Language.es;

  private readonly userConfigKey = 'userConfig';
  readonly config$ = new BehaviorSubject<UserConfig>(this.emptyConfig());

  constructor() {
    this.loadConfig();
  }

  private loadConfig(): void {
    // TODO: We clean the old lang key. This line must be removed in the future.
    localStorage.removeItem('lang');

    let config = this.emptyConfig();
    const localUserConfig = localStorage.getItem(this.userConfigKey);
    if (localUserConfig) {
      const localConfig: UserConfig = JSON.parse(localUserConfig);
      if (this.isValid(localConfig)) {
        config = localConfig;
      }
    }

    this.config$.next(config);
  }

  private emptyConfig(): UserConfig {
    return new UserConfig(this.LANG_DEFAULT);
  }

  private isValid(config: UserConfig): boolean {
    return config != null && config.lang != null;
  }

  get lang(): Language {
    return this.config$.getValue().lang;
  }

  set lang(lang: Language) {
    const currentConfig = this.config$.getValue();
    const newConfig = { ...currentConfig, lang: lang };

    localStorage.setItem(this.userConfigKey, JSON.stringify(newConfig));
    this.config$.next(newConfig);
  }
}
