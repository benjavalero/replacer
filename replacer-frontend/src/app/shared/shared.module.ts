import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbAlertModule } from '@ng-bootstrap/ng-bootstrap';
import { AlertContainerComponent } from './alert/alert-container.component';
import { AlertComponent } from './alert/alert.component';
import { AlertService } from './alert/alert.service';

@NgModule({
  declarations: [AlertComponent, AlertContainerComponent],
  exports: [AlertComponent, AlertContainerComponent],
  imports: [CommonModule, FontAwesomeModule, NgbAlertModule],
  providers: [AlertService]
})
export class SharedModule {}
