import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { SharedModule } from '../shared/shared.module';
import { StatsComponent } from './stats.component';
import { StatsService } from './stats.service';

@NgModule({
  declarations: [StatsComponent],
  imports: [CommonModule, SharedModule],
  providers: [StatsService]
})
export class StatsModule {}
