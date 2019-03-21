import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../environments/environment';

import { DumpStatus } from './dump-status';
import { interval } from 'rxjs';

@Component({
  selector: 'app-dump',
  templateUrl: './dump.component.html',
  styleUrls: ['./dump.component.css']
})
export class DumpComponent implements OnInit {
  status: DumpStatus;
  submitted = false;

  constructor(private httpClient: HttpClient) {}

  ngOnInit() {
    this.findDumpStatus();

    // Refresh every 10 seconds
    interval(10000).subscribe(() => this.findDumpStatus());
  }

  private findDumpStatus() {
    console.log('Find dump status');
    this.httpClient
      .get<DumpStatus>(`${environment.apiUrl}/dump/status`)
      .subscribe(res => {
        this.status = res;
      });
  }

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
}
