import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { Observable, Subscription, interval } from 'rxjs';
import { AlertComponent } from '../../shared/alerts/alert-container/alert/alert.component';
import { sleep } from '../../shared/util/sleep';
import { DumpAdapterService } from './dump-adapter.service';
import { DumpAdapterStatus } from './dump.model';

@Component({
  standalone: true,
  selector: 'app-dump',
  imports: [CommonModule, FormsModule, AlertComponent],
  providers: [DumpAdapterService],
  templateUrl: './dump.component.html',
  styleUrls: []
})
export class DumpComponent implements OnInit, OnDestroy {
  // Status Details
  status$!: Observable<DumpAdapterStatus | null>;

  // Check the status
  subscription!: Subscription;

  constructor(
    private dumpService: DumpAdapterService,
    private titleService: Title
  ) {}

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
