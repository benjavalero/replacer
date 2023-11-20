import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './core/header/header.component';
import { AlertContainerComponent } from './shared/alert/alert-container.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [HeaderComponent, AlertContainerComponent, CommonModule, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'replacer-frontend';
}
