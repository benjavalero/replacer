import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdministrationService } from '../api/services/administration.service';
import { ReplacementService } from '../api/services/replacement.service';
import { SharedModule } from '../shared/shared.module';
import { AdminRoutingModule } from './admin-routing.module';
import { AdminComponent } from './admin.component';
import { DumpIndexingAdapterService } from './dump-indexing/dump-indexing-adapter.service';
import { DumpIndexingComponent } from './dump-indexing/dump-indexing.component';
import { PublicIpComponent } from './public-ip/public-ip.component';
import { UnreviewedComponent } from './unreviewed/unreviewed.component';

@NgModule({
  declarations: [AdminComponent, DumpIndexingComponent, PublicIpComponent, UnreviewedComponent],
  imports: [CommonModule, RouterModule, FormsModule, AdminRoutingModule, SharedModule],
  providers: [AdministrationService, DumpIndexingAdapterService, ReplacementService]
})
export class AdminModule {}
