import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Observable } from 'rxjs';
import { sleep } from '../sleep';
import { DumpIndexing } from './dump-indexing.model';
import { DumpIndexingService } from './dump-indexing.service';

@Component({
  selector: 'app-dump',
  templateUrl: './dump-indexing.component.html',
  styleUrls: []
})
export class DumpIndexingComponent implements OnInit {
  // Status Details
  status$: Observable<DumpIndexing>;

  constructor(private dumpService: DumpIndexingService, private titleService: Title) {
    this.status$ = this.dumpService.status$;
  }

  ngOnInit() {
    this.titleService.setTitle('Replacer - Indexación');
  }

  onSubmit() {
    this.dumpService.startDumpIndexing$().subscribe(() => {
      // It takes a little for the back-end to set the running status
      sleep(10000).then(() => this.dumpService.refreshDumpIndexing());
    });
  }
}
