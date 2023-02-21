import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  standalone: true,
  selector: 'app-change-language',
  templateUrl: './change-language.component.html',
  styles: []
})
export class ChangeLanguageComponent {
  constructor(public activeModal: NgbActiveModal) {}
}
