import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../shared/shared.module';
import { AdminComponent } from './admin.component';
import { AdminService } from './admin.service';
import { DumpIndexingComponent } from './dump-indexing/dump-indexing.component';
import { DumpIndexingService } from './dump-indexing/dump-indexing.service';
import { PublicIpComponent } from './public-ip/public-ip.component';
import { UnreviewedComponent } from './unreviewed/unreviewed.component';
import { UnreviewedService } from './unreviewed/unreviewed.service';

@NgModule({
  declarations: [AdminComponent, DumpIndexingComponent, PublicIpComponent, UnreviewedComponent],
  imports: [CommonModule, RouterModule, FormsModule, HttpClientModule, SharedModule],
  providers: [AdminService, DumpIndexingService, UnreviewedService]
})
export class AdminModule {}
