import { Component, OnInit } from '@angular/core';
import { AlertService } from '../alert/alert.service';

@Component({
  selector: 'app-find-random',
  template: ``,
  styleUrls: []
})
export class FindRandomComponent implements OnInit {

  constructor(private alertService: AlertService) { }

  ngOnInit() {
    this.alertService.addAlertMessage({
      type: 'primary',
      message: 'Buscando artículo aleatorio con reemplazos…'
    });
  }

}
