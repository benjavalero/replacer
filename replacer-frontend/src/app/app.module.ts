import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgbAlertModule, NgbCollapseModule, NgbDropdownModule, NgbModalModule, NgbPaginationModule } from '@ng-bootstrap/ng-bootstrap';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { AuthenticationGuard } from './guard/authentication.guard';
import { LangInterceptor } from './interceptor/lang-interceptor';
import { UserInterceptor } from './interceptor/user-interceptor';

import { AlertContainerComponent } from './alert/alert-container.component';
import { AlertComponent } from './alert/alert.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { DashboardComponent } from './dashboard/dashboard.component';
import { LoginComponent } from './authentication/login.component';
import { OAuthResponseComponent } from './authentication/oauth-response.component';
import { HeaderComponent } from './header/header.component';
import { FindRandomComponent } from './page/find-random.component';
import { FindCustomComponent } from './page/find-custom.component';
import { ValidateCustomComponent } from './page/validate-custom.component';
import { EditPageComponent } from './page/edit-page.component';
import { EditSnippetComponent } from './page/edit-snippet.component';
import { DumpIndexingComponent } from './dump-indexing/dump-indexing.component';
import { ReplacementListComponent } from './replacement-list/replacement-list.component';
import { ReplacementTableComponent } from './replacement-list/replacement-table.component';
import { ReviewSubtypeComponent } from './replacement-list/review-subtype.component';
import { StatsComponent } from './stats/stats.component';

@NgModule({
  declarations: [
    AppComponent,
    AlertContainerComponent,
    AlertComponent,
    DashboardComponent,
    LoginComponent,
    OAuthResponseComponent,
    HeaderComponent,
    FindRandomComponent,
    FindCustomComponent,
    ValidateCustomComponent,
    EditPageComponent,
    EditSnippetComponent,
    DumpIndexingComponent,
    ReplacementListComponent,
    ReplacementTableComponent,
    ReviewSubtypeComponent,
    StatsComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    AppRoutingModule,
    FontAwesomeModule,
    NgbAlertModule,
    NgbCollapseModule,
    NgbDropdownModule,
    NgbModalModule,
    NgbPaginationModule
  ],
  providers: [
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
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
