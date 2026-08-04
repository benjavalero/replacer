import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { environment } from '../environments/environment';
import { provideApiConfiguration } from './api/api-configuration';
import { routes } from './app.routes';
import { errorInterceptor } from './core/interceptors/error-interceptor';
import { langInterceptor } from './core/interceptors/lang-interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZonelessChangeDetection(),
    provideRouter(routes),
    provideApiConfiguration(environment.baseUrl),
    provideHttpClient(withInterceptors([langInterceptor, errorInterceptor]))
  ]
};
