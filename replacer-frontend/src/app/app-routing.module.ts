import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { LoginComponent } from './login/login.component';
import { StatsComponent } from './stats/stats.component';
import { DumpComponent } from './dump/dump.component';
import { RandomComponent } from './random/random.component';
import { AuthGuard } from './guards/auth-guard.service';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'stats', component: StatsComponent, canActivate: [AuthGuard] },
  { path: 'dump', component: DumpComponent, canActivate: [AuthGuard] },
  { path: 'random', component: RandomComponent, canActivate: [AuthGuard] },
  { path: 'random/:word', component: RandomComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: '/dashboard', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
