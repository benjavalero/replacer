import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { UserConfigService } from '../../core/services/user-config.service';
import { Snippet } from './page-replacement.model';

@Component({
  standalone: true,
  selector: 'app-edit-custom-snippet',
  templateUrl: './edit-custom-snippet.component.html',
  styleUrls: ['./edit-custom-snippet.component.css']
})
export class EditCustomSnippetComponent implements OnInit {
  @Input() snippet!: Snippet;
  private newText!: string;
  userLang = this.userConfigService.lang;

  constructor(
    public activeModal: NgbActiveModal,
    private userConfigService: UserConfigService
  ) {}

  ngOnInit(): void {
    this.newText = this.snippet.text;
  }

  onChange(event: any) {
    this.newText = event.target.innerText;
  }

  accept(): void {
    const newSnippet = { ...this.snippet, text: this.newText };
    this.activeModal.close(newSnippet);
  }
}
