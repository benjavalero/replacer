import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Observable } from 'rxjs';
import { KindCount } from './replacement-list.model';
import { ReplacementListService } from './replacement-list.service';

@Component({
  selector: 'app-replacement-list',
  templateUrl: './replacement-list.component.html',
  styleUrls: []
})
export class ReplacementListComponent implements OnInit {
  kindCounts$!: Observable<KindCount[] | null>;

  constructor(private titleService: Title, private replacementService: ReplacementListService) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Lista de reemplazos');
    this.replacementService.loadCountsFromServer();
    this.kindCounts$ = this.replacementService.counts$;
  }
}
