import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { Observable } from 'rxjs';
import { AlertComponent } from '../../shared/alert/alert.component';
import { User } from '../user/user.model';
import { UserService } from '../user/user.service';

@Component({
  standalone: true,
  selector: 'app-dashboard',
  imports: [CommonModule, RouterModule, AlertComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: []
})
export class DashboardComponent implements OnInit {
  user$!: Observable<User | null>;

  constructor(private userService: UserService, private titleService: Title) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Reemplazador de la Wikipedia');

    this.user$ = this.userService.user$;
  }
}
