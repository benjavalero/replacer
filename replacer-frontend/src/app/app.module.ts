import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { DashboardComponent } from './dashboard/dashboard.component';
import { HeaderComponent } from './header/header.component';
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
    DashboardComponent,
    HeaderComponent,
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
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {}
