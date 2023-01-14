import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './core/authentication/login.component';
import { OAuthResponseComponent } from './core/authentication/oauth-response.component';
import { DashboardComponent } from './core/dashboard/dashboard.component';
import { AuthenticationGuard } from './core/guard/authentication.guard';
import { FindCustomComponent } from './review/page/find-custom.component';
import { FindRandomComponent } from './review/page/find-random.component';
import { ReplacementListComponent } from './review/replacement-list/replacement-list.component';

const routes: Routes = [
  { path: '', component: OAuthResponseComponent },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthenticationGuard] },
  { path: 'random', component: FindRandomComponent, canActivate: [AuthenticationGuard] },
  { path: 'random/:id', component: FindRandomComponent, canActivate: [AuthenticationGuard] },
  { path: 'random/:kind/:subtype/:suggestion', component: FindRandomComponent, canActivate: [AuthenticationGuard] }, // Backward-compatibility
  { path: 'custom', component: FindCustomComponent, canActivate: [AuthenticationGuard] },
  { path: 'custom/:subtype/:suggestion/:cs', component: FindRandomComponent, canActivate: [AuthenticationGuard] },
  { path: 'custom/:subtype/:suggestion/:cs/:id', component: FindRandomComponent, canActivate: [AuthenticationGuard] },
  { path: 'admin', loadChildren: () => import('./admin/admin.module').then((m) => m.AdminModule) },
  { path: 'list', component: ReplacementListComponent, canActivate: [AuthenticationGuard] },
  { path: 'list/:kind/:subtype', component: FindRandomComponent, canActivate: [AuthenticationGuard] },
  { path: 'list/:kind/:subtype/:id', component: FindRandomComponent, canActivate: [AuthenticationGuard] },
  { path: 'stats', loadChildren: () => import('./stats/stats.module').then((m) => m.StatsModule) },
  { path: '**', redirectTo: '/dashboard', pathMatch: 'full' }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      onSameUrlNavigation: 'reload',
    })
  ],
  exports: [RouterModule],
  providers: []
})
export class AppRoutingModule {}
