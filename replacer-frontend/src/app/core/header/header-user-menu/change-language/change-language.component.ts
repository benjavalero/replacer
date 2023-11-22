import { ChangeDetectionStrategy, Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-change-language',
  standalone: true,
  templateUrl: './change-language.component.html',
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChangeLanguageComponent {
  constructor(public activeModal: NgbActiveModal) {}
}
