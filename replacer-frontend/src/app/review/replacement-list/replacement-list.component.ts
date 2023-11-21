import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Observable } from 'rxjs';
import { KindCount } from '../../api/models/kind-count';
import { AlertComponent } from '../../shared/alerts/alert-container/alert/alert.component';
import { ReplacementListService } from './replacement-list.service';
import { ReplacementTableComponent } from './replacement-table.component';

@Component({
  standalone: true,
  selector: 'app-replacement-list',
  imports: [CommonModule, AlertComponent, ReplacementTableComponent],
  templateUrl: './replacement-list.component.html',
  styleUrls: []
})
export class ReplacementListComponent implements OnInit {
  kindCounts$!: Observable<KindCount[] | null>;

  constructor(
    private titleService: Title,
    private replacementListService: ReplacementListService
  ) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Lista de reemplazos');
    this.replacementListService.loadCountsFromServer();
    this.kindCounts$ = this.replacementListService.counts$;
  }
}
