import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbPaginationModule } from '@ng-bootstrap/ng-bootstrap';
import { SharedModule } from '../shared/shared.module';
import { EditCustomSnippetComponent } from './page/edit-custom-snippet.component';
import { EditPageComponent } from './page/edit-page.component';
import { EditSnippetComponent } from './page/edit-snippet.component';
import { FindCustomComponent } from './page/find-custom.component';
import { FindRandomComponent } from './page/find-random.component';
import { PageService } from './page/page.service';
import { ValidateCustomComponent } from './page/validate-custom.component';
import { ReplacementListComponent } from './replacement-list/replacement-list.component';
import { ReplacementListService } from './replacement-list/replacement-list.service';
import { ReplacementTableComponent } from './replacement-list/replacement-table.component';
import { ReviewSubtypeComponent } from './replacement-list/review-subtype.component';

@NgModule({
  declarations: [
    ReplacementListComponent,
    ReplacementTableComponent,
    ReviewSubtypeComponent,
    FindRandomComponent,
    EditPageComponent,
    EditSnippetComponent,
    FindCustomComponent,
    EditCustomSnippetComponent,
    ValidateCustomComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    HttpClientModule,
    SharedModule,
    FontAwesomeModule,
    NgbPaginationModule
  ],
  providers: [ReplacementListService, PageService]
})
export class ReviewModule {}
