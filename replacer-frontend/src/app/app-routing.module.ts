import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { LoginComponent } from './login/login.component';
import { StatsComponent } from './stats/stats.component';
import { DumpComponent } from './dump/dump.component';
import { RandomComponent } from './random/random.component';
import { AuthenticationGuard } from './authentication/authentication.guard';

const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthenticationGuard] },
  /*
  { path: 'stats', component: StatsComponent, canActivate: [AuthenticationGuard] },
  { path: 'dump', component: DumpComponent, canActivate: [AuthenticationGuard] },
  { path: 'random', component: RandomComponent, canActivate: [AuthenticationGuard] },
  { path: 'random/:word', component: RandomComponent, canActivate: [AuthenticationGuard] },
  { path: '**', redirectTo: '/dashboard', pathMatch: 'full' }
  */
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
