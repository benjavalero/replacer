import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthenticationGuard } from '../core/guard/authentication.guard';
import { StatsComponent } from './stats.component';

const routes: Routes = [{ path: '', component: StatsComponent, canActivate: [AuthenticationGuard] }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StatsRoutingModule {}
