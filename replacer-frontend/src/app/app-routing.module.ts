import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthenticationGuard } from './guard/authentication.guard';
import { DashboardComponent } from './dashboard/dashboard.component';
import { OAuthResponseComponent } from './authentication/oauth-response.component';
import { LoginComponent } from './authentication/login.component';
import { FindRandomComponent } from './page/find-random.component';
import { FindCustomComponent } from './page/find-custom.component';
import { EditPageComponent } from './page/edit-page.component';
import { DumpIndexingComponent } from './dump-indexing/dump-indexing.component';
import { ReplacementListComponent } from './replacement-list/replacement-list.component';
import { StatsComponent } from './stats/stats.component';

const routes: Routes = [
  { path: '', component: OAuthResponseComponent },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthenticationGuard] },
  { path: 'random', component: FindRandomComponent, canActivate: [AuthenticationGuard] },
  { path: 'random/:type/:subtype', component: FindRandomComponent, canActivate: [AuthenticationGuard] },
  { path: 'random/:type/:subtype/:suggestion', component: FindRandomComponent, canActivate: [AuthenticationGuard] },
  { path: 'custom', component: FindCustomComponent, canActivate: [AuthenticationGuard] },
  { path: 'article/:id', component: EditPageComponent, canActivate: [AuthenticationGuard] },
  { path: 'article/:id/:type/:subtype', component: EditPageComponent, canActivate: [AuthenticationGuard] },
  { path: 'article/:id/:type/:subtype/:suggestion', component: EditPageComponent, canActivate: [AuthenticationGuard] },
  { path: 'dump', component: DumpIndexingComponent, canActivate: [AuthenticationGuard] },
  { path: 'replacement', component: ReplacementListComponent, canActivate: [AuthenticationGuard] },
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
