import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './core/header/header.component';
import { AlertContainerComponent } from './shared/alert/alert-container.component';

@Component({
  standalone: true,
  selector: 'app-root',
  imports: [HeaderComponent, AlertContainerComponent, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {}
