import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  standalone: true,
  selector: 'app-validate-custom',
  templateUrl: './existing-custom.component.html',
  styles: []
})
export class ExistingCustomComponent {
  @Input() kindLabel!: string;
  @Input() subtype!: string;

  constructor(public activeModal: NgbActiveModal) {}
}
