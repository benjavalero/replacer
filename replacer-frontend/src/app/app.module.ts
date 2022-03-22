import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AdminModule } from './admin/admin.module';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { ReviewModule } from './review/review.module';
import { SharedModule } from './shared/shared.module';
import { StatsModule } from './stats/stats.module';

@NgModule({
  declarations: [AppComponent],
  imports: [BrowserModule, AppRoutingModule, CoreModule, SharedModule, StatsModule, AdminModule, ReviewModule],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {}
