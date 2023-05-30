import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchColorModalComponent } from './search-color-modal.component';

describe('SearchColorModalComponent', () => {
  let component: SearchColorModalComponent;
  let fixture: ComponentFixture<SearchColorModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SearchColorModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchColorModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
