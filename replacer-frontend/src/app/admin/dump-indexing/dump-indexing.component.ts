import { Component, OnDestroy, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { interval, Observable, Subscription } from 'rxjs';
import { sleep } from '../../shared/util/sleep';
import { DumpIndexingAdapterService } from './dump-indexing-adapter.service';
import { DumpIndexingAdapterStatus } from './dump-indexing.model';

@Component({
  selector: 'app-dump',
  templateUrl: './dump-indexing.component.html',
  styleUrls: []
})
export class DumpIndexingComponent implements OnInit, OnDestroy {
  // Status Details
  status$!: Observable<DumpIndexingAdapterStatus | null>;

  // Check the status
  subscription!: Subscription;

  constructor(private dumpService: DumpIndexingAdapterService, private titleService: Title) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Indexación');
    this.status$ = this.dumpService.status$;

    // Refresh every 10 seconds
    this.subscription = interval(10000).subscribe(() => {
      this.dumpService.refreshDumpIndexing();
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
  }
}
