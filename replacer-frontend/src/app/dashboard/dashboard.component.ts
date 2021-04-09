import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Observable } from 'rxjs';
import { User } from '../user/user.model';
import { UserService } from '../user/user.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: []
})
export class DashboardComponent implements OnInit {
  user$: Observable<User>;

  constructor(private userService: UserService, private titleService: Title) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Reemplazador de la Wikipedia');

    this.user$ = this.userService.user$;
  }
}
