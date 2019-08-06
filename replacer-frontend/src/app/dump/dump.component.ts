import { Component, OnInit } from '@angular/core';

import { interval } from 'rxjs';
import { DumpService } from './dump.service';
import { DumpStatus } from './dump-status.model';
import { AlertService } from '../alert/alert.service';
import { sleep } from '../sleep';

const NUM_ARTICLES = 3801980; // Rough amount of articles to be read

@Component({
  selector: 'app-dump',
  templateUrl: './dump.component.html',
  styleUrls: []
})
export class DumpComponent implements OnInit {
  // Status Details
  status: DumpStatus;
  elapsed: string;
  progress: number;
  average: number;
  eta: string;

  // Form
  force: boolean;

  constructor(private dumpService: DumpService, private alertService: AlertService) { }

  ngOnInit() {
    this.alertService.addInfoMessage('Cargando estado de la indexación…');
    this.findDumpStatus();

    // Refresh every 10 seconds
    interval(10000).subscribe(() => this.findDumpStatus());
  }

  private findDumpStatus() {
    this.dumpService.findDumpStatus().subscribe((status: DumpStatus) => {
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
    if (this.status.numArticlesRead) {
      // We might have more read articles than the estimation constant
      return this.status.numArticlesRead * 100.0 / Math.max(NUM_ARTICLES, this.status.numArticlesRead);
    } else {
      return 0;
    }
  }

  private calculateAverage(): number {
    if (this.status.numArticlesRead) {
      return this.calculateElapsed() / this.status.numArticlesRead;
    } else {
      return 0;
    }
  }

  private calculateEta(): number {
    if (this.status.running) {
      const toRead = Math.max(NUM_ARTICLES, this.status.numArticlesRead) - this.status.numArticlesRead;
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

    this.dumpService.runIndexation(this.force).subscribe(() => {
      // It takes a little for the back-end to set the running status
      sleep(5000).then(() => this.findDumpStatus());
    });
  }

}
