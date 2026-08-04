import { ChangeDetectionStrategy, Component, Input, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { UserConfigService } from '../../core/services/user-config.service';
import { Snippet } from './page-replacement.model';

@Component({
  selector: 'app-edit-custom-snippet',
  templateUrl: './edit-custom-snippet.component.html',
  styleUrls: ['./edit-custom-snippet.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditCustomSnippetComponent {
  @Input() snippet!: Snippet;
  private newText!: string;

  activeModal = inject(NgbActiveModal);
  private userConfigService = inject(UserConfigService);

  userLang = this.userConfigService.lang;

  onChange(event: Event) {
    this.newText = (event.target as HTMLElement).innerText;
  }

  accept(): void {
    const newSnippet = { ...this.snippet, text: this.newText ?? this.snippet.text };
    this.activeModal.close(newSnippet);
  }
}
