import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Title } from '@angular/platform-browser';
import { interval } from 'rxjs';
import { AlertComponent } from '../../shared/alerts/alert-container/alert/alert.component';
import { sleep } from '../../shared/util/sleep';
import { DumpAdapterService } from './dump-adapter.service';

@Component({
  selector: 'app-dump',
  imports: [DecimalPipe, AlertComponent],
  providers: [DumpAdapterService],
  templateUrl: './dump.component.html',
  styleUrls: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DumpComponent {
  private dumpService = inject(DumpAdapterService);

  status = this.dumpService.status;

  constructor() {
    inject(Title).setTitle('Replacer - Indexación');

    // Refresh every 10 seconds
    interval(10000)
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.dumpService.refreshDumpIndexing());
  }

  onSubmit() {
    this.dumpService.startDumpIndexing$().subscribe(() => {
      // It takes a little for the back-end to set the running status
      sleep(10000).then(() => this.dumpService.refreshDumpIndexing());
    });
  }
}
