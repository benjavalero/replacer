import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Observable } from 'rxjs';
import { REPLACEMENT_KINDS } from './replacement-kind.model';
import { TypeCount } from './replacement-list.model';
import { ReplacementListService } from './replacement-list.service';

@Component({
  selector: 'app-replacement-list',
  templateUrl: './replacement-list.component.html',
  styleUrls: []
})
export class ReplacementListComponent implements OnInit {
  typeCounts$!: Observable<TypeCount[] | null>;

  constructor(private titleService: Title, private replacementService: ReplacementListService) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Lista de reemplazos');
    this.replacementService.loadCountsFromServer();
    this.typeCounts$ = this.replacementService.counts$;
  }

  getLabel(kind: number): string {
    return REPLACEMENT_KINDS.get(kind)!.label;
  }
}
