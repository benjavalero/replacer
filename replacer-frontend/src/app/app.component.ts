import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './core/header/header.component';
import { AlertContainerComponent } from './shared/alerts/alert-container/alert-container.component';

@Component({
  selector: 'app-root',
  imports: [HeaderComponent, AlertContainerComponent, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent {}
