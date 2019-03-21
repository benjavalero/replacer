import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { StatsComponent } from './stats/stats.component';
import { DumpComponent } from './dump/dump.component';

const routes: Routes = [
  { path: 'dashboard', component: DashboardComponent },
  { path: 'stats', component: StatsComponent },
  { path: 'dump', component: DumpComponent },
  { path: '**', redirectTo: '/dashboard', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
