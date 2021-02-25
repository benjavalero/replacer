import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

import { Suggestion } from './suggestion.model';

const THRESHOLD = 200; // Number of characters to display around the replacements

@Component({
  selector: 'app-edit-snippet',
  templateUrl: './edit-snippet.component.html',
  styleUrls: ['./edit-snippet.component.css']
})
export class EditSnippetComponent implements OnInit {
  @Input() text: string;
  @Input() start: number;
  @Input() word: string;
  @Input() suggestions: Suggestion[];

  textLeft: string;
  private suggestionSelectedValue: Suggestion;
  textRight: string;

  @Output() fixed: EventEmitter<any> = new EventEmitter();

  constructor() {}

  ngOnInit() {
    this.textLeft = this.trimLeft(this.text);
    this.textRight = this.trimRight(this.text);

    // If the original word is not in the suggestions we added it at the first position
    const posOriginalWord = this.suggestions.map((sug) => sug.text).findIndex((word) => word === this.word);
    if (posOriginalWord >= 0) {
      let originalSuggested = this.suggestions[posOriginalWord];
      if (!originalSuggested.comment) {
        originalSuggested = { ...originalSuggested, comment: 'no reemplazar' };
        this.suggestions[posOriginalWord] = originalSuggested;
      }
      this.suggestionSelected = originalSuggested;
    } else {
      this.suggestions.unshift({
        text: this.word,
        comment: 'no reemplazar'
      });
      this.suggestionSelected = this.suggestions[0];
    }
  }

  get suggestionSelected(): Suggestion {
    return this.suggestionSelectedValue;
  }

  set suggestionSelected(suggestion: Suggestion) {
    this.suggestionSelectedValue = suggestion;
    this.fixed.emit({ position: this.start, newText: suggestion.text === this.word ? null : suggestion.text });
  }

  onSelectSuggestion(index: number) {
    this.suggestionSelected = this.suggestions[index];
  }

  private trimLeft(text: string): string {
    const limitLeft = Math.max(0, this.start - THRESHOLD);
    return (limitLeft ? '[...] ' : '') + text.slice(limitLeft, this.start);
  }

  private trimRight(text: string): string {
    const end = this.start + this.word.length;
    const limitRight = Math.min(end + THRESHOLD, text.length);
    return text.slice(end, limitRight) + (limitRight === text.length ? '' : ' [...]');
  }
}
