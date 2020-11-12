import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthenticationGuard } from './authentication/authentication.guard';
import { DashboardComponent } from './dashboard/dashboard.component';
import { LoginComponent } from './login/login.component';
import { FindRandomComponent } from './page/find-random.component';
import { FindCustomComponent } from './page/find-custom.component';
import { EditPageComponent } from './page/edit-page.component';
import { DumpComponent } from './dump/dump.component';
import { FindReplacementComponent } from './replacement/find-replacement.component';
import { StatsComponent } from './stats/stats.component';

const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthenticationGuard] },
  { path: 'random', component: FindRandomComponent, canActivate: [AuthenticationGuard] },
  { path: 'random/:type/:subtype', component: FindRandomComponent, canActivate: [AuthenticationGuard] },
  { path: 'random/:type/:subtype/:suggestion', component: FindRandomComponent, canActivate: [AuthenticationGuard] },
  { path: 'custom', component: FindCustomComponent, canActivate: [AuthenticationGuard] },
  { path: 'article/:id', component: EditPageComponent, canActivate: [AuthenticationGuard] },
  { path: 'article/:id/:type/:subtype', component: EditPageComponent, canActivate: [AuthenticationGuard] },
  { path: 'article/:id/:type/:subtype/:suggestion', component: EditPageComponent, canActivate: [AuthenticationGuard] },
  { path: 'dump', component: DumpComponent, canActivate: [AuthenticationGuard] },
  { path: 'replacement', component: FindReplacementComponent, canActivate: [AuthenticationGuard] },
  { path: 'stats', component: StatsComponent, canActivate: [AuthenticationGuard] },
  { path: '**', redirectTo: '/dashboard', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    onSameUrlNavigation: 'reload',
    relativeLinkResolution: 'legacy'
})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
