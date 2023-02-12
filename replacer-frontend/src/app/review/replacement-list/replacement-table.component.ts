import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { faCheckDouble, faList } from '@fortawesome/free-solid-svg-icons';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SubtypeCount } from '../../api/models/subtype-count';
import { UserConfigService } from '../../core/user/user-config.service';
import { User } from '../../core/user/user.model';
import { UserService } from '../../core/user/user.service';
import StringUtils from '../../shared/util/string-utils';
import { kindLabel } from '../page/find-random.component';
import { ReplacementListService } from './replacement-list.service';
import { ReviewSubtypeComponent } from './review-subtype.component';
import { rotate, SortDirection } from './sort-direction.model';

@Component({
  selector: 'app-replacement-table',
  templateUrl: './replacement-table.component.html',
  styleUrls: ['./replacement-table.component.css']
})
export class ReplacementTableComponent implements OnInit, OnChanges {
  private readonly PAGE_SIZE = 8;
  private readonly MAX_SIZE = 3;

  @Input() kind!: number;
  @Input() subtypeCounts: SubtypeCount[];

  filteredItems: SubtypeCount[];

  user$!: Observable<User | null>;
  label!: string;

  // Filters
  sortColumn: string;
  sortDirection: SortDirection;
  filterValue: string;
  collectionSize: number;
  pageSize = this.PAGE_SIZE;
  maxSize = this.MAX_SIZE;
  pageValue: number;

  // Subtype Review
  pageListUrl: string;
  listIcon = faList;
  checkIcon = faCheckDouble;

  constructor(
    private userConfigService: UserConfigService,
    private userService: UserService,
    private replacementListService: ReplacementListService,
    private modalService: NgbModal
  ) {
    this.subtypeCounts = [];
    this.filteredItems = this.subtypeCounts;

    // Default filter
    this.sortColumn = 's';
    this.sortDirection = 'asc';
    this.filterValue = '';
    this.collectionSize = this.subtypeCounts.length;
    this.pageValue = 1;

    this.pageListUrl = `${environment.baseUrl}/api/page/type?lang=${this.userConfigService.lang}&user=${this.userService.userName}`;
  }

  ngOnInit() {
    this.user$ = this.userService.user$;
    this.label = kindLabel[this.kind];
  }

  ngOnChanges() {
    this.sortAndRefresh();
  }

  private sortAndRefresh(): void {
    // The sorting can be done in the original counts to speed up next filters without sorting
    this.subtypeCounts = this.sortCounts(this.subtypeCounts, this.sortColumn, this.sortDirection);

    this.refreshFilteredItems();
  }

  private refreshFilteredItems(): void {
    const filtered = this.filterCounts(this.subtypeCounts, this.filterValue);
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

    this.sortAndRefresh();
  }

  columnDirection(column: string): SortDirection {
    return column === this.sortColumn ? this.sortDirection : '';
  }

  private sortCounts(items: SubtypeCount[], column: string, direction: SortDirection): SubtypeCount[] {
    return [...items].sort((a, b) => {
      const aValue: any = a[column as keyof SubtypeCount];
      const bValue: any = b[column as keyof SubtypeCount];
      const res = column === 's' ? StringUtils.compareStringBase(aValue, bValue) : this.compareNumber(aValue, bValue);
      return direction === 'asc' ? res : -res;
    });
  }

  private compareNumber(v1: number, v2: number): number {
    if (v1 < v2) {
      return -1;
    } else if (v1 > v2) {
      return 1;
    } else {
      return 0;
    }
  }

  private filterCounts(items: SubtypeCount[], text: string): SubtypeCount[] {
    const filterText = StringUtils.removeDiacritics(text);
    return items.filter((item) =>
      StringUtils.removeDiacritics(item.s).includes(StringUtils.removeDiacritics(filterText))
    );
  }

  private paginateCounts(items: SubtypeCount[], page: number, pageSize: number): SubtypeCount[] {
    if (items.length <= pageSize) {
      return items;
    } else {
      return items.slice((page - 1) * pageSize, (page - 1) * pageSize + pageSize);
    }
  }

  get filtrable(): boolean {
    return this.subtypeCounts.length > this.PAGE_SIZE;
  }

  reviewPages(subtype: string): void {
    const modalRef = this.modalService.open(ReviewSubtypeComponent);
    modalRef.componentInstance.kind = this.label;
    modalRef.componentInstance.subtype = subtype;
    modalRef.result.then(
      (result) => {
        this.replacementListService.reviewSubtype$(this.kind, subtype).subscribe(() => {
          // Nothing to do
        });
      },
      (reason) => {
        // Nothing to do
      }
    );
  }
}
