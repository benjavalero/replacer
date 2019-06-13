import { Component, OnInit } from '@angular/core';

import { interval } from 'rxjs';
import { DumpService } from './dump.service';
import { DumpStatus } from './dump-status.model';

const NUM_ARTICLES = 3718238; // Rough amount of articles to be read

@Component({
  selector: 'app-dump',
  templateUrl: './dump.component.html',
  styleUrls: []
})
export class DumpComponent implements OnInit {
  status: DumpStatus;
  // submitted = false;
  elapsed: number;
  progress: number;
  average: number;

  constructor(private dumpService: DumpService) { }

  ngOnInit() {
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

  /*
  onSubmit() {
    this.runIndexation(this.status.forceProcess);
  }

  private runIndexation(forceProcess: boolean) {
    const force = forceProcess ? '/force' : '';
    this.httpClient
      .get<boolean>(`${environment.apiUrl}/dump/run${force}`)
      .subscribe(res => {
        this.status.running = true;
        this.findDumpStatus();
      });
  }
  */
}
