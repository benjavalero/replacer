import { Injectable, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { DumpStatus } from '../../api/models/dump-status';
import { DumpApiService } from '../../api/services/dump-api.service';
import { DumpAdapterStatus } from './dump.model';

@Injectable()
export class DumpAdapterService {
  private dumpApiService = inject(DumpApiService);

  // Initial state is null when there is no data yet
  readonly status = signal<DumpAdapterStatus | null>(null);

  constructor() {
    this.refreshDumpIndexing();
  }

  refreshDumpIndexing(): void {
    this.dumpApiService.getDumpStatus().subscribe((status: DumpStatus | null) => {
      if (status === null) {
        // As a trick we add an empty dump status
        this.status.set({});
      } else {
        // The calculations could be done with asynchronous pipes,
        // but it is not worth as the calculations are quite simple
        this.status.set({
          ...status,
          startDate: this.formatDate(status.start),
          endDate: this.formatDate(status.end),
          elapsed: this.formatMilliseconds(this.calculateElapsed(status)),
          progress: this.calculateProgress(status),
          average: this.calculateAverage(status),
          eta: this.formatMilliseconds(this.calculateEta(status))
        });
      }
    });
  }

  startDumpIndexing$(): Observable<void> {
    // Empty the last indexation
    this.status.set(null);
    return this.dumpApiService.manualStartDumpIndexing();
  }

  private formatDate(milliseconds?: number): Date | undefined {
    return milliseconds ? new Date(milliseconds) : undefined;
  }

  private formatMilliseconds(milliseconds: number): string {
    const totalSeconds = Math.floor(milliseconds / 1000);
    const seconds = (totalSeconds % 60).toFixed().padStart(2, '0');
    const totalMinutes = Math.floor(totalSeconds / 60);
    const minutes = (totalMinutes % 60).toFixed().padStart(2, '0');
    const totalHours = Math.floor(totalMinutes / 60);
    return `${totalHours}:${minutes}:${seconds}`;
  }

  private calculateElapsed(status: DumpStatus): number {
    if (status.start) {
      return status.end ? status.end - status.start : Date.now() - status.start;
    }
    return 0;
  }

  private calculateProgress(status: DumpStatus): number {
    if (status.numPagesRead && status.numPagesEstimated) {
      // We might have more read pages than the estimation constant
      return (status.numPagesRead * 100.0) / Math.max(status.numPagesEstimated, status.numPagesRead);
    }
    return 0;
  }

  private calculateAverage(status: DumpStatus): number {
    return status.numPagesRead ? this.calculateElapsed(status) / status.numPagesRead : 0;
  }

  private calculateEta(status: DumpStatus): number {
    if (status.running && status.numPagesRead && status.numPagesEstimated) {
      const toRead = Math.max(status.numPagesEstimated, status.numPagesRead) - status.numPagesRead;
      return toRead * this.calculateAverage(status);
    }
    return 0;
  }
}
