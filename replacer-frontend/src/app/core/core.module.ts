import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NgbCollapseModule, NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { environment } from '../../environments/environment';
import { ApiModule } from '../api/api.module';
import { SharedModule } from '../shared/shared.module';
import { LoginComponent } from './authentication/login.component';
import { LoginService } from './authentication/login.service';
import { OAuthResponseComponent } from './authentication/oauth-response.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AuthenticationGuard } from './guard/authentication.guard';
import { ChangeLanguageComponent } from './header/change-language.component';
import { HeaderComponent } from './header/header.component';
import { LangInterceptor } from './interceptor/lang-interceptor';
import { UserInterceptor } from './interceptor/user-interceptor';
import { UserConfigService } from './user/user-config.service';
import { UserService } from './user/user.service';

@NgModule({
  declarations: [DashboardComponent, HeaderComponent, ChangeLanguageComponent, LoginComponent, OAuthResponseComponent],
  exports: [HeaderComponent],
  imports: [
    CommonModule,
    RouterModule,
    SharedModule,
    ApiModule.forRoot({ rootUrl: environment.baseUrl }),
    NgbCollapseModule,
    NgbDropdownModule
  ],
  providers: [
    UserService,
    UserConfigService,
    LoginService,
    AuthenticationGuard,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: LangInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: UserInterceptor,
      multi: true
    }
  ]
})
export class CoreModule {}
