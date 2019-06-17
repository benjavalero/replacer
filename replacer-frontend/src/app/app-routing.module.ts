import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { LoginComponent } from './login/login.component';
import { FindRandomComponent } from './article/find-random.component';
import { EditArticleComponent } from './article/edit-article.component';
import { DumpComponent } from './dump/dump.component';
import { AuthenticationGuard } from './authentication/authentication.guard';

const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthenticationGuard] },
  { path: 'random', component: FindRandomComponent, canActivate: [AuthenticationGuard] },
  { path: 'article/:id', component: EditArticleComponent, canActivate: [AuthenticationGuard] },
  { path: 'dump', component: DumpComponent, canActivate: [AuthenticationGuard] },
  /*
  { path: 'stats', component: StatsComponent, canActivate: [AuthenticationGuard] },
  { path: 'random/:word', component: RandomComponent, canActivate: [AuthenticationGuard] },
  */
  { path: '**', redirectTo: '/dashboard', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
