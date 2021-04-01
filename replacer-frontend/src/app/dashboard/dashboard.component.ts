import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { UserService } from '../user/user.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: []
})
export class DashboardComponent implements OnInit {
  admin$: Observable<boolean>;

  constructor(private userService: UserService, private titleService: Title) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Reemplazador de la Wikipedia');

    this.admin$ = this.userService.user$.pipe(map((user) => user.admin));
  }
}
