import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { AuthenticationGuard } from './authentication/authentication.guard';

import { AlertContainerComponent } from './alert/alert-container.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { LoginComponent } from './login/login.component';
import { HeaderComponent } from './header/header.component';
import { FindRandomComponent } from './article/find-random.component';
import { FindCustomComponent } from './article/find-custom.component';
import { EditArticleComponent } from './article/edit-article.component';
import { EditSnippetComponent } from './article/edit-snippet.component';
import { DumpComponent } from './dump/dump.component';
import { FindReplacementComponent } from './replacement/find-replacement.component';
import { ReplacementTableComponent } from './replacement/replacement-table.component';
import { ColumnSortableDirective } from './replacement/column-sortable.directive';
import { StatsComponent } from './stats/stats.component';

@NgModule({
  declarations: [
    AppComponent,
    AlertContainerComponent,
    DashboardComponent,
    LoginComponent,
    HeaderComponent,
    FindRandomComponent,
    FindCustomComponent,
    EditArticleComponent,
    EditSnippetComponent,
    DumpComponent,
    FindReplacementComponent,
    ReplacementTableComponent,
    ColumnSortableDirective,
    StatsComponent
  ],
  entryComponents: [],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    AppRoutingModule,
    NgbModule
  ],
  providers: [AuthenticationGuard],
  bootstrap: [AppComponent]
})
export class AppModule { }
