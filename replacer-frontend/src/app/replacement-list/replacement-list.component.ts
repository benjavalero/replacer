import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Observable } from 'rxjs';
import { AlertService } from '../alert/alert.service';
import { ReplacementCountList } from './replacement-list.model';
import { ReplacementListService } from './replacement-list.service';

@Component({
  selector: 'app-replacement-list',
  templateUrl: './replacement-list.component.html',
  styleUrls: []
})
export class ReplacementListComponent implements OnInit {
  counts$: Observable<ReplacementCountList[]>;

  constructor(
    private alertService: AlertService,
    private titleService: Title,
    private replacementService: ReplacementListService
  ) {
    this.counts$ = this.replacementService.counts$;
  }

  ngOnInit() {
    this.alertService.clearAlertMessages();
    this.titleService.setTitle('Replacer - Lista de reemplazos');
  }
}
