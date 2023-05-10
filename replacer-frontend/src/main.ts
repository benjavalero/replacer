/// <reference types="@angular/localize" />

import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { importProvidersFrom } from '@angular/core';
import { bootstrapApplication, BrowserModule } from '@angular/platform-browser';
import { ApiModule } from './app/api/api.module';
import { AppRoutingModule } from './app/app-routing.module';
import { AppComponent } from './app/app.component';
import { AuthInterceptor } from './app/core/interceptor/auth-interceptor';
import { LangInterceptor } from './app/core/interceptor/lang-interceptor';
import { environment } from './environments/environment';

bootstrapApplication(AppComponent, {
  providers: [
    importProvidersFrom(BrowserModule, AppRoutingModule, ApiModule.forRoot({ rootUrl: environment.baseUrl })),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: LangInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    provideHttpClient(withInterceptorsFromDi())
  ]
}).catch((err) => console.error(err));
