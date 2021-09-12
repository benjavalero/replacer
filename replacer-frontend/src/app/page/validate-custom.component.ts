import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-validate-custom',
  templateUrl: './validate-custom.component.html',
  styles: []
})
export class ValidateCustomComponent {
  @Input() type!: string;
  @Input() subtype!: string;

  constructor(public activeModal: NgbActiveModal) {}
}
