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
import { EditArticleComponent } from './article/edit-article.component';
import { StatsComponent } from './stats/stats.component';
import { MisspellingTableComponent } from './misspelling-table/misspelling-table.component';
import { SortableDirective } from './misspelling-table/sortable.directive';
import { DumpComponent } from './dump/dump.component';
import { RandomComponent } from './random/random.component';
import { MisspellingReplacerComponent } from './misspelling-replacer/misspelling-replacer.component';
import {
  ContentEditorComponent,
  replacerComponents
} from './content-editor/content-editor.component';

@NgModule({
  declarations: [
    AppComponent,
    AlertContainerComponent,
    DashboardComponent,
    LoginComponent,
    HeaderComponent,
    FindRandomComponent,
    EditArticleComponent,
    StatsComponent,
    MisspellingTableComponent,
    SortableDirective,
    DumpComponent,
    RandomComponent,
    ContentEditorComponent,
    replacerComponents,
    MisspellingReplacerComponent
  ],
  entryComponents: [replacerComponents],
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
