import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MisspellingTableComponent } from './misspelling-table.component';

describe('MisspellingTableComponent', () => {
  let component: MisspellingTableComponent;
  let fixture: ComponentFixture<MisspellingTableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MisspellingTableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MisspellingTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
