import { Component, OnDestroy, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { interval, Subscription } from 'rxjs';
import { AlertService } from '../alert/alert.service';
import { sleep } from '../sleep';
import { DumpIndexingStatus } from './dump-indexing-status.model';
import { DumpIndexingService } from './dump-indexing.service';

@Component({
  selector: 'app-dump',
  templateUrl: './dump.component.html',
  styleUrls: []
})
export class DumpComponent implements OnInit, OnDestroy {
  // Status Details
  status: DumpIndexingStatus;
  elapsed: string;
  progress: number;
  average: number;
  eta: string;

  // Check the status
  subscription: Subscription;

  constructor(
    private dumpService: DumpIndexingService,
    private alertService: AlertService,
    private titleService: Title
  ) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Indexación');
    this.alertService.addInfoMessage('Cargando estado de la indexación…');
    this.getDumpIndexingStatus();

    // Refresh every 10 seconds
    this.subscription = interval(10000).subscribe(() => this.getDumpIndexingStatus());
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  private getDumpIndexingStatus() {
    this.dumpService.getDumpIndexingStatus().subscribe((status: DumpIndexingStatus) => {
      this.status = status;
      this.elapsed = this.formatMilliseconds(this.calculateElapsed());
      this.progress = this.calculateProgress();
      this.average = this.calculateAverage();
      this.eta = this.formatMilliseconds(this.calculateEta());

      this.alertService.clearAlertMessages();
    });
  }

  private calculateElapsed(): number {
    if (this.status.start) {
      if (this.status.end) {
        return this.status.end - this.status.start;
      } else {
        return Date.now() - this.status.start;
      }
    } else {
      return 0;
    }
  }

  private formatMilliseconds(millis: number): string {
    const totalSeconds = Math.floor(millis / 1000);
    const seconds = (totalSeconds % 60).toFixed().padStart(2, '0');
    const totalMinutes = Math.floor(totalSeconds / 60);
    const minutes = (totalMinutes % 60).toFixed().padStart(2, '0');
    const totalHours = Math.floor(totalMinutes / 60);
    return `${totalHours}:${minutes}:${seconds}`;
  }

  private calculateProgress(): number {
    if (this.status.numPagesRead) {
      // We might have more read articles than the estimation constant
      return (this.status.numPagesRead * 100.0) / Math.max(this.status.numPagesEstimated, this.status.numPagesRead);
    } else {
      return 0;
    }
  }

  private calculateAverage(): number {
    if (this.status.numPagesRead) {
      return this.calculateElapsed() / this.status.numPagesRead;
    } else {
      return 0;
    }
  }

  private calculateEta(): number {
    if (this.status.running) {
      const toRead = Math.max(this.status.numPagesEstimated, this.status.numPagesRead) - this.status.numPagesRead;
      return toRead * this.calculateAverage();
    } else {
      return 0;
    }
  }

  formatDate(milliseconds: number): Date {
    return milliseconds ? new Date(milliseconds) : null;
  }

  onSubmit() {
    this.status = null;
    this.alertService.addInfoMessage('Iniciando indexación…');

    this.dumpService.startDumpIndexing().subscribe(() => {
      // It takes a little for the back-end to set the running status
      sleep(10000).then(() => this.getDumpIndexingStatus());
    });
  }
}
