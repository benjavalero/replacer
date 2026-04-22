import { CommonModule } from '@angular/common';
import { Component, OnInit, computed } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { UserService } from '../../core/services/user.service';
import { AlertComponent } from '../../shared/alerts/alert-container/alert/alert.component';
import { AlertService } from '../../shared/alerts/alert.service';
import StringUtils from '../../shared/util/string-utils';

@Component({
  standalone: true,
  selector: 'app-find-custom',
  imports: [CommonModule, ReactiveFormsModule, AlertComponent],
  templateUrl: './find-custom.component.html',
  styleUrls: []
})
export class FindCustomComponent implements OnInit {
  readonly customForm = this.formBuilder.nonNullable.group({
    replacement: [''],
    suggestion: [''],
    caseSensitive: [false]
  });

  readonly userLacksPermissions = computed(() => !this.userService.canUseCustomReplacement());

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly router: Router,
    private readonly alertService: AlertService,
    private readonly titleService: Title,
    private readonly userService: UserService
  ) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Reemplazo personalizado');
  }

  onSubmit() {
    const r = this.customForm.controls.replacement.value.trim();
    const s = this.customForm.controls.suggestion.value.trim();
    const cs = this.customForm.controls.caseSensitive.value;

    if (this.validate(r, s, cs)) {
      this.router.navigate([`review/custom/${r}/${s}/${cs}`]);
    }
  }

  private validate(replacement: string, suggestion: string, cs: boolean): boolean {
    this.alertService.clearAlertMessages();

    if (!replacement || !suggestion) {
      this.alertService.addErrorMessage('El reemplazo y la sugerencia son obligatorios');
      return false;
    } else if (replacement === suggestion || (!cs && StringUtils.compareStringAccent(replacement, suggestion) === 0)) {
      this.alertService.addErrorMessage('El texto a reemplazar y el sugerido son iguales');
      return false;
    }
    return true;
  }
}
