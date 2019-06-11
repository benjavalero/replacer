import { Component, OnInit, Input } from '@angular/core';
import { ReplacementSuggestion } from './replacement-suggestion.model';

@Component({
  selector: 'app-edit-snippet',
  templateUrl: './edit-snippet.component.html',
  styles: []
})
export class EditSnippetComponent implements OnInit {

  @Input() start: number;
  @Input() text: string;
  @Input() suggestions: ReplacementSuggestion[];

  constructor() { }

  ngOnInit() {
  }

}
