import { Component, OnInit, inject } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { AlertComponent } from '../../shared/alerts/alert-container/alert/alert.component';
import { ReplacementListService } from './replacement-list.service';
import { ReplacementTableComponent } from './replacement-table.component';

@Component({
  selector: 'app-replacement-list',
  imports: [AlertComponent, ReplacementTableComponent],
  templateUrl: './replacement-list.component.html',
  styleUrls: []
})
export class ReplacementListComponent implements OnInit {
  private titleService = inject(Title);
  private replacementListService = inject(ReplacementListService);

  readonly kindCounts = this.replacementListService.counts;

  ngOnInit() {
    this.titleService.setTitle('Replacer - Lista de reemplazos');
    this.replacementListService.loadCountsFromServer();
  }
}
