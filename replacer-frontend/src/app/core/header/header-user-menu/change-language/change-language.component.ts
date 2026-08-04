import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-change-language',
  templateUrl: './change-language.component.html',
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChangeLanguageComponent {
  activeModal = inject(NgbActiveModal);
}
