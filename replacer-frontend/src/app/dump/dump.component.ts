import { Component, OnInit } from '@angular/core';

import { interval } from 'rxjs';
import { DumpService } from './dump.service';
import { DumpStatus } from './dump-status.model';
import { AlertService } from '../alert/alert.service';

const NUM_ARTICLES = 3718238; // Rough amount of articles to be read

// https://flaviocopes.com/javascript-sleep/
const sleep = (milliseconds: number) => {
  return new Promise(resolve => setTimeout(resolve, milliseconds));
};

@Component({
  selector: 'app-dump',
  templateUrl: './dump.component.html',
  styleUrls: []
})
export class DumpComponent implements OnInit {
  // Status Details
  status: DumpStatus;
  elapsed: number;
  progress: number;
  average: number;

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
      this.elapsed = this.calculateElapsed();
      this.progress = this.calculateProgress();
      this.average = this.calculateAverage();

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

  private calculateProgress(): number {
    if (this.status.numArticlesRead) {
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

  formatDate(milliseconds: number): Date {
    return milliseconds ? new Date(milliseconds) : null;
  }

  onSubmit() {
    this.status = null;
    this.alertService.addInfoMessage('Iniciando indexación…');

    this.dumpService.runIndexation(this.force)
      .subscribe(
        // It takes a little for the back-end to set the running status
        () => sleep(5000).then(() => this.findDumpStatus()));
  }

}
