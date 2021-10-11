import { Component, Input, OnChanges } from '@angular/core';
import { faCheckDouble, faList } from '@fortawesome/free-solid-svg-icons';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { environment } from '../../environments/environment';
import { UserConfigService } from '../user/user-config.service';
import { UserService } from '../user/user.service';
import { ReplacementCount } from './replacement-list.model';
import { ReplacementListService } from './replacement-list.service';
import { ReviewSubtypeComponent } from './review-subtype.component';
import { rotate, SortDirection } from './sort-direction.model';

@Component({
  selector: 'app-replacement-table',
  templateUrl: './replacement-table.component.html',
  styleUrls: ['./replacement-table.component.css']
})
export class ReplacementTableComponent implements OnChanges {
  private readonly PAGE_SIZE = 8;
  private readonly MAX_SIZE = 3;

  @Input() type!: string;
  @Input() replacementCounts: ReplacementCount[];

  filteredItems: ReplacementCount[];

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
    this.replacementCounts = [];
    this.filteredItems = this.replacementCounts;

    // Default filter
    this.sortColumn = 's';
    this.sortDirection = 'asc';
    this.filterValue = '';
    this.collectionSize = this.replacementCounts.length;
    this.pageValue = 1;

    this.pageListUrl = `${environment.apiUrl}/pages?lang=${this.userConfigService.lang}&user=${this.userService.userName}`;
  }

  ngOnChanges() {
    this.sortAndRefresh();
  }

  private sortAndRefresh(): void {
    // The sorting can be done in the original counts to speed up next filters without sorting
    this.replacementCounts = this.sortCounts(this.replacementCounts, this.sortColumn, this.sortDirection);

    this.refreshFilteredItems();
  }

  private refreshFilteredItems(): void {
    const filtered = this.filterCounts(this.replacementCounts, this.filterValue);
    const paginated = this.paginateCounts(filtered, this.pageValue, this.pageSize);

    this.collectionSize = filtered.length;
    this.filteredItems = paginated;
  }

  private removeDiacritics(text: string): string {
    return text
      .trim()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase();
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

  private sortCounts(items: ReplacementCount[], column: string, direction: SortDirection): ReplacementCount[] {
    return [...items].sort((a, b) => {
      const aValue: any = a[column as keyof ReplacementCount];
      const bValue: any = b[column as keyof ReplacementCount];
      const res = column === 's' ? this.compareString(aValue, bValue) : this.compareNumber(aValue, bValue);
      return direction === 'asc' ? res : -res;
    });
  }

  private compareNumber(v1: number, v2: number): number {
    return v1 < v2 ? -1 : v1 > v2 ? 1 : 0;
  }

  private compareString(v1: string, v2: string): number {
    return v1.localeCompare(v2, 'es', { sensitivity: 'base' });
  }

  private filterCounts(items: ReplacementCount[], text: string): ReplacementCount[] {
    const filterText = this.removeDiacritics(text);
    return items.filter((item) => this.removeDiacritics(item.s).includes(this.removeDiacritics(filterText)));
  }

  private paginateCounts(items: ReplacementCount[], page: number, pageSize: number): ReplacementCount[] {
    if (items.length <= pageSize) {
      return items;
    } else {
      return items.slice((page - 1) * pageSize, (page - 1) * pageSize + pageSize);
    }
  }

  get filtrable(): boolean {
    return this.replacementCounts.length > this.PAGE_SIZE;
  }

  reviewPages(subtype: string): void {
    const modalRef = this.modalService.open(ReviewSubtypeComponent);
    modalRef.componentInstance.type = this.type;
    modalRef.componentInstance.subtype = subtype;
    modalRef.result.then(
      (result) => {
        this.replacementListService.reviewSubtype$(this.type, subtype).subscribe(() => {
          // Nothing to do
        });
      },
      (reason) => {
        // Nothing to do
      }
    );
  }
}
