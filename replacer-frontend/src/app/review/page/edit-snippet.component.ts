import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output, ViewEncapsulation } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Replacement } from '../../api/models/replacement';
import { Suggestion } from '../../api/models/suggestion';
import { EditCustomSnippetComponent } from './edit-custom-snippet.component';
import { FixedReplacement, getReplacementEnd, Snippet } from './page-replacement.model';

@Component({
  standalone: true,
  selector: 'app-edit-snippet',
  imports: [CommonModule],
  templateUrl: './edit-snippet.component.html',
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./edit-snippet.component.css']
})
export class EditSnippetComponent implements OnInit {
  @Input() index!: number;
  @Input() pageText!: string;
  @Input() replacement!: Replacement;

  // Limits on left and right to edit the snippet as we would clash with other replacements
  @Input() limitLeft!: number;
  @Input() limitRight!: number;

  private suggestionSelectedValue!: Suggestion | null;
  customFix: Snippet | null;

  @Output() fixed: EventEmitter<FixedReplacement> = new EventEmitter();

  constructor(private modalService: NgbModal) {
    this.customFix = null;
  }

  ngOnInit() {
    // The first suggestion may match with the original text
    if (this.replacement.text === this.replacement.suggestions[0].text && !this.replacement.suggestions[0].comment) {
      this.replacement.suggestions[0].comment = 'no reemplazar';
    }

    this.suggestionSelected = this.replacement.suggestions[0];
  }

  get textLeft(): string {
    return this.trimLeft().text;
  }

  get textRight(): string {
    return this.trimRight().text;
  }

  get suggestionSelected(): Suggestion | null {
    return this.suggestionSelectedValue;
  }

  set suggestionSelected(suggestion: Suggestion | null) {
    this.suggestionSelectedValue = suggestion;
    this.customFix = null;
    const fixedReplacement = new FixedReplacement(
      this.index,
      this.replacement.start,
      this.replacement.text,
      suggestion && suggestion.text !== this.replacement.text ? suggestion.text : null
    );
    this.fixed.emit(fixedReplacement);
  }

  onSelectSuggestion(index: number) {
    this.suggestionSelected = this.replacement.suggestions[index];
  }

  private trimLeft(): Snippet {
    const snippetText = this.pageText.slice(this.limitLeft, this.replacement.start);
    return new Snippet(this.limitLeft, snippetText);
  }

  private trimRight(): Snippet {
    const snippetText = this.pageText.slice(getReplacementEnd(this.replacement), this.limitRight);
    return new Snippet(getReplacementEnd(this.replacement), snippetText);
  }

  onEdit(): void {
    const modalRef = this.modalService.open(EditCustomSnippetComponent, { windowClass: 'snippet-modal' });
    const editableSnippet = this.customFix ? this.customFix : this.buildEditableSnippet();
    modalRef.componentInstance.snippet = editableSnippet;
    modalRef.result.then(
      (result: Snippet) => {
        if (editableSnippet.text !== result.text) {
          this.suggestionSelectedValue = null;
          this.customFix = result;

          const fixedReplacement = new FixedReplacement(this.index, result.start, this.getOriginalText(), result.text);
          this.fixed.emit(fixedReplacement);
        }
      },
      (reason) => {
        // Nothing to do
      }
    );
  }

  private buildEditableSnippet(): Snippet {
    return new Snippet(this.limitLeft, this.textLeft + this.suggestionSelected!.text + this.textRight);
  }

  private getOriginalText(): string {
    return this.textLeft + this.replacement.text + this.textRight;
  }
}
