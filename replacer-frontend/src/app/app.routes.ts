import { Routes } from '@angular/router';
import { AdminComponent } from './admin/admin.component';
import { OAuthResponseComponent } from './core/authentication/oauth-response.component';
import { DashboardComponent } from './core/dashboard/dashboard.component';
import { authenticationGuard } from './core/guard/authentication.guard';
import { FindCustomComponent } from './review/page/find-custom.component';
import { FindRandomComponent } from './review/page/find-random.component';
import { ReplacementListComponent } from './review/replacement-list/replacement-list.component';
import { ReplacementListService } from './review/replacement-list/replacement-list.service';
import { StatsComponent } from './stats/stats.component';

export const routes: Routes = [
  { path: '', component: OAuthResponseComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authenticationGuard] },

  // Backward-compatibility
  { path: 'random/Personalizado/:subtype/:suggestion', redirectTo: 'custom/:subtype/:suggestion/false' },
  { path: 'random/Ortografía/:subtype', redirectTo: 'review/2/:subtype' },
  { path: 'random/Compuestos/:subtype', redirectTo: 'review/3/:subtype' },
  { path: 'custom', redirectTo: 'review/custom' },

  { path: 'admin', component: AdminComponent, canActivate: [authenticationGuard] },
  {
    path: 'review',
    providers: [ReplacementListService],
    canActivate: [authenticationGuard],
    canActivateChild: [authenticationGuard],
    children: [
      { path: 'custom', component: FindCustomComponent },
      { path: 'custom/:subtype/:suggestion/:cs', component: FindRandomComponent },
      { path: 'custom/:subtype/:suggestion/:cs/:id', component: FindRandomComponent },
      { path: 'list', component: ReplacementListComponent },
      { path: ':kind/:subtype', component: FindRandomComponent },
      { path: ':kind/:subtype/:id', component: FindRandomComponent },
      { path: ':id', component: FindRandomComponent },
      { path: '', component: FindRandomComponent }
    ]
  },
  { path: 'stats', component: StatsComponent, canActivate: [authenticationGuard] },
  { path: '**', redirectTo: '/dashboard', pathMatch: 'full' }
];
