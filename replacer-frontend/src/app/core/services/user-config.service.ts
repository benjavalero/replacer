import { computed, Injectable, signal } from '@angular/core';
import { Language } from '../model/language.model';
import { UserConfig } from './user-config.model';

@Injectable({
  providedIn: 'root'
})
export class UserConfigService {
  private readonly LANG_DEFAULT: Language = Language.SPANISH;

  private readonly userConfigKey = 'userConfig';

  readonly config = signal<UserConfig>(this.loadConfig());
  readonly lang = computed(() => this.config().lang);

  private loadConfig(): UserConfig {
    let config = this.emptyConfig();
    const localUserConfig = localStorage.getItem(this.userConfigKey);
    if (localUserConfig !== null) {
      const localConfig: UserConfig = JSON.parse(localUserConfig);
      if (this.isValid(localConfig)) {
        config = localConfig;
      }
    }
    return config;
  }

  private emptyConfig(): UserConfig {
    return new UserConfig(this.LANG_DEFAULT);
  }

  private isValid(config: UserConfig): boolean {
    return config != null && config.lang != null;
  }

  setLang(lang: Language) {
    this.config.update((c) => ({ ...c, lang: lang }));
    localStorage.setItem(this.userConfigKey, JSON.stringify(this.config()));
  }
}
