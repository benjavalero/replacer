import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DumpIndexingStatus } from './dump-indexing.model';

@Injectable()
export class DumpIndexingService {
  private readonly baseUrl = `${environment.apiUrl}/dump-indexing`;

  // Initial state is null when there is no data yet
  readonly status$ = new Subject<DumpIndexingStatus | null>();

  constructor(private httpClient: HttpClient) {
    this.refreshDumpIndexing();
  }

  refreshDumpIndexing(): void {
    this.getDumpIndexing$().subscribe((status: DumpIndexingStatus) => {
      // The calculations could done with asynchronous pipes
      // but it is not worth as the calculations are quite simple
      const startDate = this.formatDate(status.start);
      const endDate = this.formatDate(status.end);
      const elapsed = this.formatMilliseconds(this.calculateElapsed(status));
      const progress = this.calculateProgress(status);
      const average = this.calculateAverage(status);
      const eta = this.formatMilliseconds(this.calculateEta(status));

      const newStatus: DumpIndexingStatus = {
        ...status,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        elapsed: elapsed,
        progress: progress,
        average: average,
        eta: eta
      };
      this.status$.next(newStatus);
    });
  }

  private getDumpIndexing$(): Observable<DumpIndexingStatus> {
    return this.httpClient.get<DumpIndexingStatus>(`${this.baseUrl}`);
  }

  startDumpIndexing$(): Observable<void> {
    // Empty the last indexation
    this.status$.next(null);

    return this.httpClient.post<void>(`${this.baseUrl}`, null);
  }

  private formatDate(milliseconds: number | undefined): Date | null {
    return milliseconds ? new Date(milliseconds) : null;
  }

  private formatMilliseconds(milliseconds: number): string {
    const totalSeconds = Math.floor(milliseconds / 1000);
    const seconds = (totalSeconds % 60).toFixed().padStart(2, '0');
    const totalMinutes = Math.floor(totalSeconds / 60);
    const minutes = (totalMinutes % 60).toFixed().padStart(2, '0');
    const totalHours = Math.floor(totalMinutes / 60);
    return `${totalHours}:${minutes}:${seconds}`;
  }

  private calculateElapsed(status: DumpIndexingStatus): number {
    if (status.start) {
      if (status.end) {
        return status.end - status.start;
      } else {
        return Date.now() - status.start;
      }
    } else {
      return 0;
    }
  }

  private calculateProgress(status: DumpIndexingStatus): number {
    if (status.numPagesRead && status.numPagesEstimated) {
      // We might have more read pages than the estimation constant
      return (status.numPagesRead * 100.0) / Math.max(status.numPagesEstimated, status.numPagesRead);
    } else {
      return 0;
    }
  }

  private calculateAverage(status: DumpIndexingStatus): number {
    if (status.numPagesRead) {
      return this.calculateElapsed(status) / status.numPagesRead;
    } else {
      return 0;
    }
  }

  private calculateEta(status: DumpIndexingStatus): number {
    if (status.running && status.numPagesRead && status.numPagesEstimated) {
      const toRead = Math.max(status.numPagesEstimated, status.numPagesRead) - status.numPagesRead;
      return toRead * this.calculateAverage(status);
    } else {
      return 0;
    }
  }
}
