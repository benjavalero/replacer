import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';

import { AlertService } from '../alert/alert.service';
import { ReplacementService } from './replacement.service';
import { ReplacementCountList } from './replacement-count-list.model';

@Component({
  selector: 'app-find-replacement',
  templateUrl: './find-replacement.component.html',
  styleUrls: []
})
export class FindReplacementComponent implements OnInit {

  replacementCountLists: ReplacementCountList[];

  constructor(private alertService: AlertService, private titleService: Title, private replacementService: ReplacementService) {
    this.replacementCountLists = [];
  }

  ngOnInit() {
    this.alertService.clearAlertMessages();
    this.titleService.setTitle('Replacer - Lista de reemplazos');
    this.alertService.addInfoMessage('Cargando estadísticas de reemplazos…');

    this.findReplacementCounts();
  }

  private findReplacementCounts() {
    this.replacementService.findReplacementCounts().subscribe((replacementCountLists: ReplacementCountList[]) => {
      this.replacementCountLists = replacementCountLists;
      this.alertService.clearAlertMessages();
    });
  }

}
