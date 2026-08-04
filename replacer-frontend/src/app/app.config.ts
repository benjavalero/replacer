import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { environment } from '../environments/environment';
import { provideApiConfiguration } from './api/api-configuration';
import { routes } from './app.routes';
import { errorInterceptor } from './core/interceptors/error-interceptor';
import { langInterceptor } from './core/interceptors/lang-interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideApiConfiguration(environment.baseUrl),
    provideHttpClient(withInterceptors([langInterceptor, errorInterceptor]))
  ]
};
