import { NgClass } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, effect, inject, input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NgbModal, NgbPaginationModule, NgbTooltip } from '@ng-bootstrap/ng-bootstrap';
import { environment } from '../../../environments/environment';
import { SubtypeCount } from '../../api/models/subtype-count';
import { UserConfigService } from '../../core/services/user-config.service';
import { UserService } from '../../core/services/user.service';
import StringUtils from '../../shared/util/string-utils';
import { replacementKindLabels as kindLabel } from '../page/replacement-kind-labels.const';
import { ReplacementListService } from './replacement-list.service';
import { ReviewSubtypeComponent } from './review-subtype.component';
import { SortDirection, rotate } from './sort-direction.model';

@Component({
  selector: 'app-replacement-table',
  imports: [NgClass, FormsModule, RouterLink, NgbPaginationModule, NgbTooltip],
  templateUrl: './replacement-table.component.html',
  styleUrls: ['./replacement-table.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReplacementTableComponent {
  private readonly PAGE_SIZE = 8;
  private readonly MAX_SIZE = 3;

  kind = input.required<number>();
  subtypeCounts = input<SubtypeCount[]>([]);

  filteredItems: SubtypeCount[] = [];

  private userConfigService = inject(UserConfigService);
  private userService = inject(UserService);
  private replacementListService = inject(ReplacementListService);
  private modalService = inject(NgbModal);

  isBotUser = this.userService.isBotUser;
  label = computed(() => kindLabel[this.kind()]);
  pageListUrl = computed(() => `${environment.baseUrl}/api/page/type?lang=${this.userConfigService.lang()}`);

  // Filters
  private sortedCounts: SubtypeCount[] = [];
  sortColumn: string = 's';
  sortDirection: SortDirection = 'asc';
  filterValue: string = '';
  collectionSize: number = 0;
  pageSize = this.PAGE_SIZE;
  maxSize = this.MAX_SIZE;
  pageValue: number = 1;

  constructor() {
    effect(() => {
      // The sorting can be done in the original counts to speed up next filters without sorting
      this.sortedCounts = this.sortCounts(this.subtypeCounts(), this.sortColumn, this.sortDirection);
      this.refreshFilteredItems();
    });
  }

  private refreshFilteredItems(): void {
    const filtered = this.filterCounts(this.sortedCounts, this.filterValue);
    const paginated = this.paginateCounts(filtered, this.pageValue, this.pageSize);

    this.collectionSize = filtered.length;
    this.filteredItems = paginated;
  }

  onFilter(value: string) {
    this.filterValue = value;
    this.page = 1; // Reset page
    this.refreshFilteredItems();
  }

  get page(): number {
    return this.pageValue;
  }

  set page(value: number) {
    this.pageValue = value;
    this.refreshFilteredItems();
  }

  sort(column: string): void {
    this.sortDirection = rotate[this.columnDirection(column)];
    this.sortColumn = column;
    this.sortedCounts = this.sortCounts(this.sortedCounts, this.sortColumn, this.sortDirection);
    this.refreshFilteredItems();
  }

  columnDirection(column: string): SortDirection {
    return column === this.sortColumn ? this.sortDirection : '';
  }

  private sortCounts(items: SubtypeCount[], column: string, direction: SortDirection): SubtypeCount[] {
    return [...items].sort((a, b) => {
      const aValue = a[column as keyof SubtypeCount];
      const bValue = b[column as keyof SubtypeCount];
      const res =
        column === 's'
          ? StringUtils.compareStringBase(aValue as string, bValue as string)
          : this.compareNumber(aValue as number, bValue as number);
      return direction === 'asc' ? res : -res;
    });
  }

  private compareNumber(v1: number, v2: number): number {
    if (v1 < v2) return -1;
    if (v1 > v2) return 1;
    return 0;
  }

  private filterCounts(items: SubtypeCount[], text: string): SubtypeCount[] {
    const filterText = StringUtils.removeDiacritics(text);
    return items.filter((item) =>
      StringUtils.removeDiacritics(item.s).includes(StringUtils.removeDiacritics(filterText))
    );
  }

  private paginateCounts(items: SubtypeCount[], page: number, pageSize: number): SubtypeCount[] {
    if (items.length <= pageSize) return items;
    return items.slice((page - 1) * pageSize, (page - 1) * pageSize + pageSize);
  }

  get filtrable(): boolean {
    return this.subtypeCounts().length > this.PAGE_SIZE;
  }

  isEditable(count: SubtypeCount): boolean {
    return count.c > 0 && (!count.forBots || this.isBotUser());
  }

  reviewPages(subtype: string): void {
    const modalRef = this.modalService.open(ReviewSubtypeComponent);
    modalRef.componentInstance.kind = this.label();
    modalRef.componentInstance.subtype = subtype;
    modalRef.result.then(() => {
      this.replacementListService.reviewSubtype$(this.kind(), subtype).subscribe();
    });
  }
}
