import { Component } from '@angular/core';
import { DumpIndexingComponent } from './dump-indexing/dump-indexing.component';
import { UnreviewedComponent } from './unreviewed/unreviewed.component';

@Component({
  standalone: true,
  selector: 'app-admin',
  imports: [DumpIndexingComponent, UnreviewedComponent],
  templateUrl: './admin.component.html',
  styleUrls: []
})
export class AdminComponent {}
