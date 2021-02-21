import { Component, OnDestroy, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { interval, Observable, Subscription } from 'rxjs';
import { AlertService } from '../alert/alert.service';
import { sleep } from '../sleep';
import { DumpIndexing } from './dump-indexing.model';
import { DumpIndexingService } from './dump-indexing.service';

@Component({
  selector: 'app-dump',
  templateUrl: './dump-indexing.component.html',
  styleUrls: []
})
export class DumpIndexingComponent implements OnInit, OnDestroy {
  // Status Details
  status$: Observable<DumpIndexing>;

  // Check the status
  subscription: Subscription;

  constructor(
    private dumpService: DumpIndexingService,
    private alertService: AlertService,
    private titleService: Title
  ) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Indexación');
    this.status$ = this.dumpService.status$;

    // Refresh every 10 seconds
    this.subscription = interval(10000).subscribe(() => {
      this.dumpService.refreshDumpIndexing();
      this.alertService.clearAlertMessages();
    });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  onSubmit() {
    this.dumpService.startDumpIndexing$().subscribe(() => {
      // It takes a little for the back-end to set the running status
      sleep(10000).then(() => this.dumpService.refreshDumpIndexing());
    });

    this.alertService.addInfoMessage('Iniciando indexación…');
  }
}
