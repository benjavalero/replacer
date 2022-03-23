import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthenticationGuard } from '../core/guard/authentication.guard';
import { AdminComponent } from './admin.component';

const routes: Routes = [{ path: 'admin', component: AdminComponent, canActivate: [AuthenticationGuard] }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule {}
