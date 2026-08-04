import { ChangeDetectionStrategy, Component, Input, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-validate-custom',
  templateUrl: './existing-custom.component.html',
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExistingCustomComponent {
  activeModal = inject(NgbActiveModal);

  @Input() kindLabel!: string;
  @Input() subtype!: string;
}
