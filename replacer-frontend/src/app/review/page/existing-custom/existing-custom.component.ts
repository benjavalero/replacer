import { Component, Input, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  standalone: true,
  selector: 'app-validate-custom',
  templateUrl: './existing-custom.component.html',
  styles: []
})
export class ExistingCustomComponent {
  activeModal = inject(NgbActiveModal);

  @Input() kindLabel!: string;
  @Input() subtype!: string;
}
